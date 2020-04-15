package com.nter.projectg.lobby;

import com.nter.projectg.controller.MessageController;
import com.nter.projectg.model.common.Message;
import com.nter.projectg.model.common.Message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Lobby {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    private final Map<String, String> userSession = new ConcurrentHashMap<>();
    private final Map<String, String> sessionUser = new ConcurrentHashMap<>();

    public Collection<String> getUsers() {
        return Collections.unmodifiableSet(userSession.keySet());
    }

    public void add(String user, String session) {
        logger.debug("User joining: {} {}", user, this);

        userSession.put(user, session);
        sessionUser.put(session, user);

        // Broadcast notification message message to all sessions
        Message message = buildLobbyMessage(MessageType.JOIN, user);
        sendToAll(message);

        logger.info("User joined: {} {}", user, this);
    }

    public void remove(String user, String session) {
        logger.debug("User leaving: {} {}", user, this);

        userSession.remove(user);
        sessionUser.remove(session);

        // Broadcast notification message message to all sessions
        Message message = buildLobbyMessage(MessageType.LEAVE, user);
        sendToAll(message);

        logger.info("User left: {} {}", user, this);
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

    private Message buildLobbyMessage(MessageType type, String user) {
        Message message = new Message();
        message.setType(type);
        message.setSender(user);
        message.setContent(String.join(",", getUsers()));
        return message;
    }

    @Override
    public String toString() {
        return "Lobby{" +
                "userSession=" + userSession +
                ", sessionUser=" + sessionUser +
                '}';
    }

}
