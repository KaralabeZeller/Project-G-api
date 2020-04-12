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
    private Message startMessage;
    private Message gameMessage;

    // TODO replace @SendTo with messagingTemplate or lobby
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public Message sendMessage(@Payload Message message) {
        logger.info("sendMessage: Handling message: {}", message);

        String user = message.getSender();

        if (message.getType() == MessageType.START) {
            start(user);
            startMessage = message;
        } else if (message.getType() == MessageType.GAME) {
            gameMessage = message;
        } else {
            // TODO
        }

        // Broadcast START or GAME or other message
        return message;
    }

    // TODO replace @SendTo with messagingTemplate or lobby
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public Message addUser(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        logger.info("addUser: Handling message: {}", message);

        String user = message.getSender();
        String session = headerAccessor.getSessionId();

        // TODO remove - username in web socket session
        headerAccessor.getSessionAttributes().put("username", user);

        // Update lobby
        lobby.add(user, session);

        // Handle reconnection
        reconnect(user, session);

        // Broadcast JOIN message
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

            logger.debug("Starting Secret Hitler game: {}", lobby);

            // TODO implement
            game = new SecretHitlerGame(lobby);
            // game.start(user);

            logger.info("Started Secret Hitler game: {}", game);
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
                logger.debug("Reconnecting user in new session: {} {}", user, session);

                // TODO implement
                // game.reconnect(user);
                if (startMessage != null) {
                    lobby.sendToUser(user, startMessage);
                    if (gameMessage != null) {
                        lobby.sendToUser(user, gameMessage);
                    }
                }

                logger.info("Reconnected user in new session: {} {}", user, session);
            }
        });
    }

}
