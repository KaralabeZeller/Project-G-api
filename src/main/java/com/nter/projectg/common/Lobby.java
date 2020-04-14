package com.nter.projectg.common;

import com.nter.projectg.controller.MessageController;
import com.nter.projectg.model.common.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Lobby {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    private final Map<String, String> userSession = new ConcurrentHashMap<>();
    private final Map<String, String> sessionUser = new ConcurrentHashMap<>();

    public void add(String user, String session) {
        logger.debug("User joining: {} {}", user, this);

        userSession.put(user, session);
        sessionUser.put(session, user);

        // Broadcast notification message message to all sessions
        Message message = new Message();
        message.setType(Message.MessageType.JOIN);
        message.setSender(user);
        message.setContent(String.join(",", getUsers()));
        sendToAll(message);

        logger.info("User joined: {} {}", user, this);
    }

    public void remove(String user, String session) {
        logger.debug("User leaving: {} {}", user, this);

        userSession.remove(user);
        sessionUser.remove(session);

        // Broadcast notification message message to all sessions
        Message message = new Message();
        message.setType(Message.MessageType.LEAVE);
        message.setSender(user);
        message.setContent(String.join(",", getUsers()));
        sendToAll(message);

        logger.info("User left: {} {}", user, this);
    }

    public Collection<String> getUsers() {
        return userSession.keySet();
    }

    public void sendToAll(Object message) {
        logger.debug("sendToAll: {}", message);

        // Broadcast message to all sessions
        messagingTemplate.convertAndSend("/topic/public", message);
    }

    public void sendToUser(String user, Object message) {
        logger.debug("sendToUser: {} {}", user, message);

        String session = userSession.get(user);

        // Send message to session
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
