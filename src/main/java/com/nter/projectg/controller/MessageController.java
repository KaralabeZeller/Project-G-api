package com.nter.projectg.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import com.nter.projectg.common.Lobby;
import com.nter.projectg.games.common.Game;
import com.nter.projectg.games.secrethitler.SecretHitler;
import com.nter.projectg.model.GMessage;
import com.nter.projectg.model.GMessage.MessageType;

import java.util.concurrent.CompletableFuture;

@Controller
public class MessageController {

	private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

	@Autowired
	private SimpMessageSendingOperations messagingTemplate;

	private Game game;
	private GMessage startMessage;

	@MessageMapping("/chat.sendMessage")
	@SendTo("/topic/public")
	public GMessage sendMessage(@Payload GMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {

		if(chatMessage.getType() == MessageType.START) {
			startMessage = chatMessage;
			game = new SecretHitler();
		}

		return chatMessage;
	}

	@MessageMapping("/chat.addUser")
	@SendTo("/topic/public")
	public GMessage addUser(@Payload GMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
		// Add username in web socket session
		headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());

		Lobby.addUser(chatMessage.getSender(), headerAccessor.getSessionId());
		logger.info("Users: " + Lobby.size());

		reconnect(chatMessage, headerAccessor.getSessionId());

		String message = "";
		for (String user : Lobby.getUsers()) {
			message += user + ",";

		}
		chatMessage.setContent(message.substring(0, message.length() - 1));

		return chatMessage;
	}

    private CompletableFuture<Void> reconnect(@Payload GMessage chatMessage, String sessionId) {
        // Fake asynchronous computation
        logger.info("User Reconnected : " + chatMessage.getSender());
        return CompletableFuture.runAsync(() -> {
            // Fake delay
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {

            }

            if (game != null /* && startMessage != null &&  startMessageHeaderAccessor != null */) {
                        GMessage reconnectMessage = startMessage;
                        SimpMessageHeaderAccessor reconnectMessageHeaderAccessor = SimpMessageHeaderAccessor.create();
                        reconnectMessageHeaderAccessor.setSessionId(sessionId);
                        reconnectMessageHeaderAccessor.setLeaveMutable(true);
                        messagingTemplate.convertAndSendToUser(sessionId, "/topic/public", reconnectMessage, reconnectMessageHeaderAccessor.getMessageHeaders());
                    }
                }
        );
    }

}
