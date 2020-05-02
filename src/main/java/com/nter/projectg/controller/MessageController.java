package com.nter.projectg.controller;

import com.nter.projectg.games.common.Game;
import com.nter.projectg.games.common.GameHandler;
import com.nter.projectg.games.common.util.Constants.GameName;
import com.nter.projectg.lobby.LobbyHandler;
import com.nter.projectg.model.common.Message;
import com.nter.projectg.model.common.Message.MessageType;
import com.nter.projectg.model.lobby.CallMessage;
import com.nter.projectg.model.lobby.LobbyMessage;
import com.nter.projectg.model.lobby.LobbyMessage.LobbyMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Controller
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private LobbyHandler lobbyHandler;

    @Autowired
    GameHandler gameFactory;

    @MessageMapping("/lobby/{lobbyId}")
    public void receiveLobby(@Payload LobbyMessage message, SimpMessageHeaderAccessor headerAccessor) {
        logger.info("receiveLobby: Received message: {} {}", message, headerAccessor);

        String session = headerAccessor.getSessionId();
        String user = message.getSender();
        String lobby = message.getLobby();

        LobbyMessageType type = message.getLobbyType();
        if (type == LobbyMessageType.JOIN) {
            // TODO avoid storing username in WebSocket / STOMP session
            Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("username", user);

            // Update lobby and broadcast notification message
            lobbyHandler.add(lobby, user, session);

            // Handle reconnection and restore state
            reconnect(user, session);
        } else {
            logger.warn("receiveLobby: Unexpected message: {}", message);
        }
    }

    @MessageMapping("/game/{lobbyId}")
    public void receiveGame(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        logger.info("receiveGame: Received message: {} {}", message, headerAccessor);

        // TODO avoid storing username in WebSocket / STOMP session
        String user = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("username");

        // Validate message
        if (!Objects.equals(user, message.getSender())) {
            logger.warn("receiveGame: Detected mismatch between user and sender: {} {}", user, message.getSender());
            return;
        }

        // Handle message and update state
        MessageType type = message.getType();
        if (type == MessageType.START) {
            start(user);
        } else if (type == MessageType.GAME) {
            process(message);
        } else {
            logger.warn("receiveGame: Unexpected message: {}", message);
        }
    }

    private CompletableFuture<Void> process(Message message) {
        // Fake asynchronous computation
        return CompletableFuture.runAsync(() -> {
            String lobby = message.getLobby();
            Game<?, ?> game = gameFactory.get(lobby);
            if (game != null) {
                logger.debug("Processing message: {} {}", message, game);
                try {
                    game.handle(message);
                    logger.info("Processed message: {} {}", message, game);
                } catch (Exception exception) {
                    logger.error("Failed to process message: {} {}", message, game, exception);
                    throw exception;
                }
            } else {
                logger.warn("Failed to process message (game not found): {}", message);
            }
        });
    }

    private CompletableFuture<Void> start(String user) {
        // Fake asynchronous computation
        return CompletableFuture.runAsync(() -> {
            lobbyHandler.findLobbyForUser(user).ifPresent(lobby -> {
                logger.debug("Starting game: Secret Hitler {}", lobby);
                Game<?, ?> game = gameFactory.createGame(GameName.SECRET_HITLER, lobby);
                game.start();
                logger.info("Started game: Secret Hitler {}", game);

                // TODO maybe move to LobbyHandler
                // Fake timeout to reset Game
                try {
                    Thread.sleep(TimeUnit.MINUTES.toMillis(10));
                } catch (InterruptedException ignored) {

                }

                logger.debug("Stopping game: Secret Hitler {}", game);
                game.stop();
                logger.debug("Stopped game: Secret Hitler {}", lobby);
            });
        });
    }

    private CompletableFuture<Void> reconnect(String user, String session) {
        // Fake asynchronous computation
        return CompletableFuture.runAsync(() -> {
            lobbyHandler.findLobbyForUser(user).ifPresent(lobby -> {
                Game<?, ?> game = gameFactory.get(lobby.getName());
                if (game != null) {
                    logger.debug("Reconnecting user in session: {} {}", user, session);
                    game.reconnect(user);
                    logger.info("Reconnected user in session: {} {}", user, session);
                }
            });
        });
    }

    @MessageMapping("/call/{lobbyId}")
    public void receiveCall(@Payload CallMessage message, SimpMessageHeaderAccessor headerAccessor) {
        logger.info("receiveCall: Received message: {} {}", message, headerAccessor);

        String user = message.getSender();
        String lobby = message.getLobby();

        CallMessage.CallMessageType type = message.getCallType();
        if (type == CallMessage.CallMessageType.ANSWER ||type == CallMessage.CallMessageType.OFFER ) {
            lobbyHandler.get(lobby).sendToAll(message);
        } else {
            logger.warn("receiveLobby: Unexpected message: {}", message);
        }

    }

}
