package com.nter.projectg.controller;

import com.nter.projectg.common.Lobby;
import com.nter.projectg.games.common.Game;
import com.nter.projectg.games.secrethitler.SecretHitler;
import com.nter.projectg.model.GMessage;
import com.nter.projectg.model.GMessage.MessageType;
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

    private Game<? extends GMessage, ? extends GameClient<? extends GMessage>> game;
    private GMessage startMessage;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public GMessage sendMessage(@Payload GMessage chatMessage) {
        logger.info("Handling message (sendMessage): {}", chatMessage);

        if (chatMessage.getType() == MessageType.START) {
            start();
            startMessage = chatMessage;
        } else {
            // TODO
        }

        // Broadcast start or other message
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public GMessage addUser(@Payload GMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        logger.info("Handling message (addUser): {}", chatMessage);

        String user = chatMessage.getSender();
        String session = headerAccessor.getSessionId();

        // TODO remove - username in web socket session
        headerAccessor.getSessionAttributes().put("username", user);

        // Update lobby
        lobby.add(user, session);
        logger.info("User joined lobby: {} {}", user, lobby);

        // Handle reconnection
        reconnect(user, session);

        // Broadcast join message
        chatMessage.setContent(String.join(",", lobby.getUsers()));
        return chatMessage;
    }

    private CompletableFuture<Void> start() {
        // Fake asynchronous computation
        return CompletableFuture.runAsync(() -> {
            // Fake delay
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {

            }

            // TODO implement - game.start();
            game = new SecretHitler(lobby);
        });
    }

    private CompletableFuture<Void> reconnect(String user, String session) {
        // Fake asynchronous computation
        return CompletableFuture.runAsync(() -> {
            // Fake delay
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {

            }

            if (game != null /* && startMessage != null &&  startMessageHeaderAccessor != null */) {
                logger.info("User reconnected in new session: {} {}", user, session);

                // TODO implement - game.reconnect(user);
                // Unicast start message
                GMessage reconnectMessage = startMessage;
                lobby.sendToUser(user, reconnectMessage);

            }
        });
    }

}
