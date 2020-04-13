package com.nter.projectg.controller;

import com.nter.projectg.common.Lobby;
import com.nter.projectg.games.common.Game;
import com.nter.projectg.games.secrethitler.SecretHitlerGame;
import com.nter.projectg.model.common.Message;
import com.nter.projectg.model.common.Message.MessageType;
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
    private Lobby lobby;

    private Game<?, ?> game;
    // TODO move to Game from MessageController
    private Message startMessage;
    private Message gameMessage;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        logger.info("sendMessage: Received message: {} {}", message, headerAccessor);

        // TODO avoid storing username in WebSocket / STOMP session
        String user = (String) headerAccessor.getSessionAttributes().get("username");

        // Validate message
        if (!Objects.equals(user, message.getSender())) {
            logger.warn("sendMessage: Detected mismatch between user and sender: {} {}", user, message.getSender());
        }

        if (message.getType() == MessageType.START) {
            start();
            startMessage = message;
        } else if (message.getType() == MessageType.GAME) {
            process(message);
            gameMessage = message;
        } else {
            // TODO other messages
        }

        // Broadcast message to all sessions
        lobby.sendToAll(message);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        logger.info("addUser: Received message: {} {}", message, headerAccessor);

        String user = message.getSender();
        String session = headerAccessor.getSessionId();

        // TODO avoid storing username in WebSocket / STOMP session
        headerAccessor.getSessionAttributes().put("username", user);

        // Update lobby and broadcast notification message
        lobby.add(user, session);

        // Handle reconnection and restore state
        reconnect(user, session);
    }

    private CompletableFuture<Void> start() {
        // Fake asynchronous computation
        return CompletableFuture.runAsync(() -> {
            logger.debug("Starting game: Secret Hitler {}", lobby);

            game = new SecretHitlerGame(lobby);
            // TODO implement
            // game.start(user);

            logger.info("Started game: Secret Hitler {}", game);

            // Fake timeout to reset Game
            try {
                Thread.sleep(TimeUnit.MINUTES.toMillis(10));
            } catch (InterruptedException ignored) {

            }

            logger.debug("Stopping game: Secret Hitler {}", game);

            // TODO implement
            // game.stop(user);
            game = null;

            logger.debug("Stopped game: Secret Hitler {}", lobby);
        });
    }

    private CompletableFuture<Void> process(Message message) {
        // Fake asynchronous computation
        return CompletableFuture.runAsync(() -> {
            logger.debug("Processing message: {} {}", message, game);
            try {
                game.process(message);
                logger.info("Processed message: {} {}", message, game);
            } catch (Exception exception) {
                logger.error("Failed to process message: {} {}", message, game, exception);
                throw exception;
            }
        });
    }

    private CompletableFuture<Void> reconnect(String user, String session) {
        // Fake asynchronous computation
        return CompletableFuture.runAsync(() -> {
            if (game != null) {
                logger.debug("Reconnecting user in session: {} {}", user, session);

                // TODO implement restore state / messages on reconnect
                if (startMessage != null) {
                    lobby.sendToUser(user, startMessage);
                    if (gameMessage != null) {
                        lobby.sendToUser(user, gameMessage);
                    }
                }

                game.reconnect(user);

                logger.info("Reconnected user in session: {} {}", user, session);
            }
        });
    }

}
