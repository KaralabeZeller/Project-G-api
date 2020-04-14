package com.nter.test.secrethitler;

import com.nter.projectg.model.common.Message;
import com.nter.projectg.model.secrethitler.SecretHitlerMessage;
import com.nter.test.common.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

// TODO extract generic functionality into Client from SecretHitlerClient
public class SecretHitlerClient extends Client {

    private static final Logger logger = LoggerFactory.getLogger(SecretHitlerClient.class);

    private StompSession.Subscription subscriptionPublic;
    private StompSession.Subscription subscriptionUser;
    private FrameHandlerPublic handlerPublic;
    private FrameHandlerUser handlerUser;

    public SecretHitlerClient(int port) {
        super(port);
    }

    @Override
    public void subscribe() {
        handlerPublic = new FrameHandlerPublic();
        handlerUser = new FrameHandlerUser();
        subscriptionPublic = getSession().subscribe("/topic/public", handlerPublic);
        subscriptionUser = getSession().subscribe("/user/topic/public", handlerUser);
    }

    @Override
    public void unsubscribe() {
        subscriptionPublic.unsubscribe();
        subscriptionUser.unsubscribe();
    }

    public void sendJoin(String user) {
        Message message = new Message();
        message.setType(Message.MessageType.JOIN);
        message.setSender(user);
        send(message);
    }

    // TODO use CompletableFuture similarly to Client#connect
    public Set<String> expectJoin() {
        Message message = handlerPublic.peekJoin();
        String[] users = message.getContent().split(",");
        return new HashSet<>(Arrays.asList(users));
    }

    private static class FrameHandlerPublic extends FrameHandler {

        private volatile Message joinMesage;

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            Message message = (Message) payload;
            logger.info("Received public message: {} {}", message, headers);

            Message.MessageType type = message.getType();
            String content = message.getContent();
            if (type == Message.MessageType.JOIN || type == Message.MessageType.LEAVE) {
                joinMesage = message;
            } else {
                // TODO other messages
            }
        }

        public Message peekJoin() {
            return joinMesage;
        }
    }

    private static class FrameHandlerUser extends FrameHandler {
        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            Message message = (Message) payload;
            logger.info("Received user message: {} {}", message, headers);

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
    }
}
