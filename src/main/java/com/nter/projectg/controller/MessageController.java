package com.nter.projectg.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.nter.projectg.common.Lobby;
import com.nter.projectg.model.GMessage;

@Controller
public class MessageController { 
	private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public GMessage sendMessage(@Payload GMessage chatMessage) {
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public GMessage addUser(@Payload GMessage chatMessage, 
                               SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        Lobby.users.add(chatMessage.getSender());
        logger.info("Users: " + Lobby.users.size());
        
        String message = "";
        for(String user: Lobby.users) {
        	message += user + ",";
        	
        }
        
        chatMessage.setContent(message.substring(0, message.length() -1));
        
        logger.info("Users: " + chatMessage.getContent());
        return chatMessage;
    }

}