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
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.util.concurrent.CompletableFuture;

@Controller
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private Lobby lobby;

    private Game<?, ?> game;
    // TODO move to Game from MessageController
    private Message startMessage;
    private Message gameMessage;

    // TODO replace @SendTo with SimpMessageSendingOperations or Lobby
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public Message sendMessage(@Payload Message message) {
        logger.info("sendMessage: Received message: {}", message);

        String user = message.getSender();

        if (message.getType() == MessageType.START) {
            start(user);
            startMessage = message;
        } else if (message.getType() == MessageType.GAME) {
            gameMessage = message;
        } else {
            // TODO other messages
        }

        // Broadcast message to all sessions
        return message;
    }

    // TODO replace @SendTo with SimpMessageSendingOperations or Lobby
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public Message addUser(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        logger.info("addUser: Received message: {}", message);

        String user = message.getSender();
        String session = headerAccessor.getSessionId();

        // TODO avoid storing username in WebSocket / STOMP session
        headerAccessor.getSessionAttributes().put("username", user);

        // Update lobby
        lobby.add(user, session);

        // Handle reconnection
        reconnect(user, session);

        // Broadcast message to all sessions
        message.setContent(String.join(",", lobby.getUsers()));
        return message;
    }

    private CompletableFuture<Void> start(String user) {
        // Fake asynchronous computation
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {

            }

            logger.debug("Starting game: Secret Hitler {}", lobby);

            game = new SecretHitlerGame(lobby);
            // TODO implement
            // game.start(user);

            logger.info("Started game: Secret Hitler {}", game);

            // Fake timeout to reset Game
            try {
                Thread.sleep(600000);
            } catch (InterruptedException ignored) {

            }

            logger.debug("Stopping game: Secret Hitler {}", game);

            // TODO implement
            game = null;

            logger.debug("Stopped game: Secret Hitler {}", lobby);
        });
    }

    private CompletableFuture<Void> reconnect(String user, String session) {
        // Fake asynchronous computation
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {

            }

            if (game != null) {
                logger.debug("Reconnecting user in session: {} {}", user, session);

                // TODO implement
                // game.reconnect(user);
                if (startMessage != null) {
                    lobby.sendToUser(user, startMessage);
                    if (gameMessage != null) {
                        lobby.sendToUser(user, gameMessage);
                    }
                }

                logger.info("Reconnected user in session: {} {}", user, session);
            }
        });
    }

}
