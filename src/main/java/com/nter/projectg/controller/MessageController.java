package com.nter.projectg.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.nter.projectg.common.Lobby;
import com.nter.projectg.games.common.Game;
import com.nter.projectg.games.secrethitler.SecretHitler;
import com.nter.projectg.model.GMessage;
import com.nter.projectg.model.GMessage.MessageType;

@Controller
public class MessageController {
	private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

	private Game game;
	
	@MessageMapping("/chat.sendMessage")
	@SendTo("/topic/public")
	public GMessage sendMessage(@Payload GMessage chatMessage) {
		
		if(chatMessage.getType() == MessageType.START)
			game = new SecretHitler();
		
		return chatMessage;
	}

	@MessageMapping("/chat.addUser")
	@SendTo("/topic/public")
	public GMessage addUser(@Payload GMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
		// Add username in web socket session
		headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
		Lobby.addUser(chatMessage.getSender());
		logger.info("Users: " + Lobby.size());

		String message = "";
		for (String user : Lobby.getUsers()) {
			message += user + ",";

		}

		chatMessage.setContent(message.substring(0, message.length() - 1));

		logger.info("Users: " + chatMessage.getContent());
		return chatMessage;
	}

}