package com.nter.projectg.controller;

import com.nter.projectg.lobby.LobbyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private LobbyHandler lobbyHandler;

    @EventListener
    public void handleWebSocketConnect(SessionConnectedEvent event) {
        logger.info("handleWebSocketConnect: Received WebSocket event: {}", event);
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        logger.info("handleWebSocketDisconnect: Received WebSocket event: {}", event);

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // TODO avoid storing username in WebSocket / STOMP session
        String user = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("username");
        String session = headerAccessor.getSessionId(); // event.getSessionId()

        if (user != null) {
            logger.debug("Disconnecting user from session: {} {}", user, session);

            // Update lobby and broadcast notification message
            lobbyHandler.remove(user, session);

            logger.info("Disconnected user from session: {} {}", user, session);
        } else {
            logger.debug("Ignoring disconnect for unknown user: {}", session);
        }
    }

}
