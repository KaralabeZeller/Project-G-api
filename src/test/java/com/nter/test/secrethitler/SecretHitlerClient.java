package com.nter.test.secrethitler;

import com.nter.projectg.model.common.Message;
import com.nter.projectg.model.secrethitler.SecretHitlerMessage;
import com.nter.test.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;

import java.util.Objects;

public class SecretHitlerClient extends Client {

    private static final Logger logger = LoggerFactory.getLogger(SecretHitlerClient.class);

    public SecretHitlerClient(int port) {
        super(port);
    }

    // TODO extract generic functionality into Client from SecretHitlerClient
    public void subscribe(StompSession session) {
        session.subscribe("/topic/public", new FrameHandler() {
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                Message message = (Message) payload;
                logger.info("Received message: {} {}", message, headers);
            }
        });

        session.subscribe("/user/topic/public", new FrameHandler() {
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                Message message = (Message) payload;
                logger.info("Received message: {} {}", message, headers);

                Message.MessageType type = message.getType();
                String content = message.getContent();
                if (type == Message.MessageType.START) {
                    logger.info("Game started");
                } else if (type == Message.MessageType.GAME) {
                    SecretHitlerMessage gameMessage = (SecretHitlerMessage) message;

                    SecretHitlerMessage.GameMessageType gameMessageType = gameMessage.getGameMessageType();
                    if (gameMessageType == SecretHitlerMessage.GameMessageType.FACTION) {
                        if (Objects.equals("FASCIST", content))
                            logger.info("You are FASCIST");
                        if (Objects.equals("LIBERAL", content))
                            logger.info("You are LIBERAL");
                        if (Objects.equals("HITLER", content))
                            logger.info("You are HITLER");
                    } else {
                        // TODO other messages
                    }
                } else {
                    // TODO other messages
                }
            }
        });
    }

    public void sendJoin(StompSession session, String user) {
        Message message = new Message();
        message.setType(Message.MessageType.JOIN);
        message.setSender(user);
        session.send("/app/chat.addUser", message);
    }

}
