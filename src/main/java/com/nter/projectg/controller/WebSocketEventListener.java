package com.nter.projectg.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.nter.projectg.common.Lobby;
import com.nter.projectg.model.GMessage;

@Component
public class WebSocketEventListener {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
	private List<String> connectedClientId = new ArrayList<>();

	@Autowired
	private SimpMessageSendingOperations messagingTemplate;

	@EventListener
	public void handleWebSocketConnectListener(SessionConnectedEvent event) {
		logger.info("Received a new web socket connection");

	}

	@EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if(username != null) {
            logger.info("User Disconnected : " + username);

            GMessage chatMessage = new GMessage();
            chatMessage.setType(GMessage.MessageType.LEAVE);
            chatMessage.setSender(username);
            
            Lobby.delUser(username);
            
            String message = "";
    		for (String user : Lobby.getUsers()) {
    			message += user + ",";

    		}
    		
    		if(message.length() > 0)
    			chatMessage.setContent(message.substring(0, message.length() - 1));


            messagingTemplate.convertAndSend("/topic/public", chatMessage);            
        }
        
    }
}