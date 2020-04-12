package com.nter.projectg.common;

import com.nter.projectg.controller.MessageController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class Lobby {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    private final Map<String, String> userSession = new HashMap<>();
    private final Map<String, String> sessionUser = new HashMap<>();

    public void add(String user, String session) {
        userSession.put(user, session);
        sessionUser.put(session, user);
        logger.info("User joined: {} {}", user, this);
    }

    public void remove(String user, String session) {
        userSession.remove(user);
        sessionUser.remove(session);
        logger.info("User left: {} {}", user, this);
    }

    public Collection<String> getUsers() {
        return userSession.keySet();
    }

    public void sendToAll(Object message) {


        logger.debug("sendToAll: {}", message);

        // Broadcast message
        messagingTemplate.convertAndSend("/topic/public", message);
    }

    public void sendToUser(String user, Object message) {
        logger.debug("sendToUser: {} {}", user, message);

        String session = userSession.get(user);

        // Unicast message
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionId(session);
        headerAccessor.setLeaveMutable(true);
        messagingTemplate.convertAndSendToUser(session, "/topic/public", message, headerAccessor.getMessageHeaders());
    }

    @Override
    public String toString() {
        return "Lobby{" +
                "userSession=" + userSession +
                ", sessionUser=" + sessionUser +
                '}';
    }
}
