package com.nter.projectg.controller;

import com.nter.projectg.common.Lobby;
import com.nter.projectg.model.common.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private Lobby lobby;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("Received a new WebSocket connection: {}", event);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        logger.info("Received a WebSocket disconnection: {}", event);

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // TODO remove - username in web socket session
        String user = (String) headerAccessor.getSessionAttributes().get("username");
        String session = headerAccessor.getSessionId(); // event.getSessionId()

        if (user != null) {
            // Update lobby
            lobby.remove(user, session);

            // Broadcast LEAVE message
            Message message = new Message();
            message.setType(Message.MessageType.LEAVE);
            message.setSender(user);
            message.setContent(String.join(",", lobby.getUsers()));
            lobby.sendToAll(message);
        }

    }
}
