package com.nter.projectg.controller;

import com.nter.projectg.games.common.Game;
import com.nter.projectg.games.common.GameHandler;
import com.nter.projectg.games.common.util.Constants;
import com.nter.projectg.lobby.Lobby;
import com.nter.projectg.lobby.LobbyHandler;
import com.nter.projectg.model.common.Message;
import com.nter.projectg.model.common.Message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
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
    public void receiveLobby(@DestinationVariable String lobbyId, @Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        logger.info("receiveLobby: Received message: {} {}", message, headerAccessor);

        String user = message.getSender();
        String lobby = message.getLobby();
        String session = headerAccessor.getSessionId();

        // TODO avoid storing username in WebSocket / STOMP session
        headerAccessor.getSessionAttributes().put("username", user);

        // Update lobby and broadcast notification message
        lobbyHandler.add(user, session, lobby);

        // Handle reconnection and restore state
        reconnect(user, session);
    }

    @MessageMapping("/game/{lobbyId}")
    public void receiveGame(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        logger.info("receiveGame: Received message: {} {}", message, headerAccessor);

        // TODO avoid storing username in WebSocket / STOMP session
        String user = (String) headerAccessor.getSessionAttributes().get("username");

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
            Game<?, ?> game = gameFactory.get(message.getLobby());

            logger.debug("Processing message: {} {}", message, game);
            try {
                game.handle(message);
                logger.info("Processed message: {} {}", message, game);
            } catch (Exception exception) {
                logger.error("Failed to  process message: {} {}", message, game, exception);
                throw exception;
            }
        });
    }

    private CompletableFuture<Void> start(String user) {
        // Fake asynchronous computation
        return CompletableFuture.runAsync(() -> {
            logger.debug("Starting game: Secret Hitler {}", lobbyHandler.findLobbyForUser(user));
            Game<?, ?> game = gameFactory.createGame(Constants.GAME_NAME.SECRET_HITLER, lobbyHandler.findLobbyForUser(user));
            game.start();
            logger.info("Started game: Secret Hitler {}", game);

            // Fake timeout to reset Game
            try {
                Thread.sleep(TimeUnit.MINUTES.toMillis(10));
            } catch (InterruptedException ignored) {

            }

            logger.debug("Stopping game: Secret Hitler {}", game);
            game.stop();
            game = null;
            logger.debug("Stopped game: Secret Hitler {}", lobbyHandler.findLobbyForUser(user));
        });
    }

    private CompletableFuture<Void> reconnect(String user, String session) {
        // Fake asynchronous computation
        return CompletableFuture.runAsync(() -> {
            Lobby l = lobbyHandler.findLobbyForUser(user);
            Game<?, ?> game = gameFactory.get(l.getName());

            if (game != null) {
                logger.debug("Reconnecting user in session: {} {}", user, session);
                game.reconnect(user);
                logger.info("Reconnected user in session: {} {}", user, session);
            }
        });
    }

}
