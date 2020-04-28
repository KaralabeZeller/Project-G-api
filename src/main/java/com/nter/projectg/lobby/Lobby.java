package com.nter.projectg.lobby;

import com.nter.projectg.model.common.Message;
import com.nter.projectg.model.common.Message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Lobby {

    private static final Logger logger = LoggerFactory.getLogger(Lobby.class);

    // Name of the lobby //TODO map to LobbyModel
    private String name;

    private SimpMessageSendingOperations messagingTemplate;

    private final Map<String, String> userSession = new ConcurrentHashMap<>();
    private final Map<String, String> sessionUser = new ConcurrentHashMap<>();

    public Lobby(String name, SimpMessageSendingOperations msgTemplate) {
        setName(name);
        this.messagingTemplate = msgTemplate;
    }

    public Collection<String> getUsers() {
        return Collections.unmodifiableSet(userSession.keySet());
    }

    public void add(String user, String session) {
        logger.debug("User joining lobby: {} {}", user, this);

        userSession.put(user, session);
        sessionUser.put(session, user);

        // Broadcast notification message message to all sessions
        Message message = buildLobbyMessage(MessageType.JOIN, user);
        sendToLobby(message);

        logger.info("User joined lobby: {} {}", user, this);
    }

    public void remove(String user, String session) {
        logger.debug("User leaving lobby: {} {}", user, this);

        userSession.remove(user);
        sessionUser.remove(session);

        // Broadcast notification message message to all sessions
        Message message = buildLobbyMessage(MessageType.LEAVE, user);
        sendToLobby(message);

        logger.info("User left lobby: {} {}", user, this);
    }

    public void sendToLobby(Object message) {
        logger.debug("sendToLobby: {}", message);

        // Broadcast message to all sessions
        messagingTemplate.convertAndSend("/topic/lobby/" + getName(), message);
    }

    public void sendToAll(Object message) {
        logger.debug("sendToAll: {}", message);

        // Broadcast message to all sessions
        messagingTemplate.convertAndSend("/topic/game/" + name, message);
    }

    public void sendToUser(String user, Object message) {
        logger.debug("sendToUser: {} {}", user, message);

        String session = userSession.get(user);

        // Send message to session
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionId(session);
        headerAccessor.setLeaveMutable(true);
        messagingTemplate.convertAndSendToUser(session, "/topic/game", message, headerAccessor.getMessageHeaders());
    }

    private Message buildLobbyMessage(MessageType type, String user) {
        Message message = new Message();
        message.setType(type);
        message.setSender(user);
        message.setLobby(getName());
        message.setContent(String.join(",", getUsers()));
        return message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getUserSession() {
        return userSession;
    }

    @Override
    public String toString() {
        return "Lobby{" +
                "name=" + name +
                "userSession=" + userSession +
                ", sessionUser=" + sessionUser +
                '}';
    }

}
