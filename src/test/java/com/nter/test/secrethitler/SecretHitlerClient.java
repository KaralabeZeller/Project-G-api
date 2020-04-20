package com.nter.test.secrethitler;

import com.nter.projectg.model.common.Message;
import com.nter.projectg.model.common.Message.MessageType;
import com.nter.projectg.model.secrethitler.SecretHitlerMessage;
import com.nter.projectg.model.secrethitler.SecretHitlerMessage.GameMessageType;
import com.nter.test.common.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SecretHitlerClient extends Client<SecretHitlerMessage> {

    private static final Logger logger = LoggerFactory.getLogger(SecretHitlerClient.class);

    private FrameHandlerLobby handlerLobby;
    private FrameHandlerPublic handlerPublic;
    private FrameHandlerUser handlerUser;

    public SecretHitlerClient(int port) {
        super(port);
    }

    @Override
    protected FrameHandlerLobby createHandlerLobby() {
        return handlerLobby = new FrameHandlerLobby();
    }

    @Override
    protected FrameHandlerPublic createHandlerPublic() {
        return handlerPublic = new FrameHandlerPublic();
    }

    @Override
    protected FrameHandlerUser createHandlerUser() {
        return handlerUser = new FrameHandlerUser();
    }

    public void sendJoin(String user) {
        Message message = buildJoinMessage(user);
        sendLobby(message);
    }

    private Message buildJoinMessage(String user) {
        Message message = new Message();
        message.setType(MessageType.JOIN);
        message.setSender(user);
        return message;
    }

    // TODO use CompletableFuture / ListenableFuture similarly to Client#connect
    public Set<String> expectJoin() {
        Message message = handlerLobby.peekJoin(); // handlerPublic.expectJoin().get()
        String[] users = message.getContent().split(",");
        return new HashSet<>(Arrays.asList(users));
    }

    private static class FrameHandlerLobby extends FrameHandler<Message> {

        private volatile Message joinMessage;

        public Message peekJoin() {
            return joinMessage;
        }

        @Override
        protected void handleOther(Message message) {
            logger.info("Received lobby message: {}", message);

            MessageType type = message.getType();
            if (type == MessageType.JOIN || type == MessageType.LEAVE) {
                joinMessage = message;
            } else {
                logger.debug("Ignoring lobby message: {}", message);
            }
        }

        @Override
        protected void handleGame(Message message) {
            logger.warn("Unexpected lobby message: {}", message);
        }

    }

    private static class FrameHandlerPublic extends FrameHandler<SecretHitlerMessage> {

        @Override
        protected void handleOther(Message message) {
            logger.info("Received public message: {}", message);

            MessageType type = message.getType();
            if (type == MessageType.START) {
                logger.info("Received start message: {}", message.getContent());
            } else {
                logger.debug("Ignoring public message: {}", message);
            }
        }

        @Override
        protected void handleGame(SecretHitlerMessage message) {
            logger.info("Received public game message: {}", message);

            GameMessageType type = message.getGameType();
            if (type == GameMessageType.FACTION) {
                logger.warn("Unexpected faction message: {}", message.getContent());
            } else {
                logger.debug("Ignoring public game message: {}", message);
            }
        }

    }

    private static class FrameHandlerUser extends FrameHandler<SecretHitlerMessage> {

        @Override
        protected void handleOther(Message message) {
            logger.info("Received user message: {}", message);

            MessageType type = message.getType();
            if (type == MessageType.START) {
                logger.info("Received start message: {}", message.getContent());
            } else {
                logger.debug("Ignoring user message: {}", message);
            }
        }

        @Override
        protected void handleGame(SecretHitlerMessage message) {
            logger.info("Received user game message: {}", message);

            GameMessageType type = message.getGameType();
            if (type == GameMessageType.FACTION) {
                logger.info("Received faction message: {}", message.getContent());
            } else {
                logger.debug("Ignoring user game message: {}", message);
            }
        }

    }

}
