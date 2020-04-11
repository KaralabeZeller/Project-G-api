package com.nter.projectg.controller;

import com.nter.projectg.common.Lobby;
import com.nter.projectg.model.GMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private Lobby lobby;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("Received a new web socket connection: {}", event);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        logger.info("Received a web socket disconnection: {}", event);

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // TODO remove - username in web socket session
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username != null) {
            // Update lobby
            lobby.remove(username, headerAccessor.getSessionId());
            logger.info("User left lobby: {} {}", username, lobby);

            // Broadcast leave message
            GMessage chatMessage = new GMessage();
            chatMessage.setType(GMessage.MessageType.LEAVE);
            chatMessage.setSender(username);
            chatMessage.setContent(String.join(",", lobby.getUsers()));

            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }

    }
}