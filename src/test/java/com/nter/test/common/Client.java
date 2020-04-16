package com.nter.test.common;

import com.nter.projectg.model.common.Message;
import com.nter.projectg.model.common.Message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.web.socket.sockjs.frame.Jackson2SockJsMessageCodec;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class Client<GameMessage extends Message> {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private final String url;

    private StompSession session;
    private StompSession.Subscription subscriptionPublic;
    private StompSession.Subscription subscriptionUser;

    public Client(int port) {
        // this.url = "wss://api.project-g.xyz:443/ws";
        this.url = "ws://localhost:" + port + "/ws";
    }

    public void connect() throws ExecutionException, InterruptedException {
        logger.debug("Connecting: {} {}", url, session);

        StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
        Transport webSocketTransport = new WebSocketTransport(webSocketClient);
        List<Transport> transports = Collections.singletonList(webSocketTransport);

        SockJsClient sockJsClient = new SockJsClient(transports);
        sockJsClient.setMessageCodec(new Jackson2SockJsMessageCodec());

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        ListenableFuture<StompSession> stompSession = stompClient.connect(url, new SessionHandler());
        session = stompSession.get();

        logger.info("Connected: {} {}", url, session);
    }

    public void disconnect() {
        logger.debug("Disconnecting: {} {}", url, session);
        try {
            session.disconnect();
            logger.info("Disconnected: {} {}", url, session);
        } catch (IllegalStateException ex) {
            logger.warn("Failed to disconnect: {} {}", url, session, ex);
        }
    }

    public void subscribe() {
        logger.debug("Subscribing: {} {}", subscriptionPublic, subscriptionUser);

        subscriptionPublic = session.subscribe("/topic/public", createHandlerPublic());
        subscriptionUser = session.subscribe("/user/topic/public", createHandlerUser());

        logger.info("Subscribed: {} {}", subscriptionPublic, subscriptionUser);
    }

    public void unsubscribe() {
        logger.debug("Unsubscribing: {} {}", subscriptionPublic, subscriptionUser);

        subscriptionPublic.unsubscribe();
        subscriptionUser.unsubscribe();

        logger.info("Unsubscribed: {} {}", subscriptionPublic, subscriptionUser);
    }

    protected abstract FrameHandler<GameMessage> createHandlerPublic();

    protected abstract FrameHandler<GameMessage> createHandlerUser();

    // TODO use /app/lobby application destination
    protected void sendAddUser(Message message) {
        logger.info("sendAddUser: {}", message);

        session.send("/app/chat.addUser", message);
    }

    // TODO use /app/game application destination
    protected void sendMessage(Message message) {
        logger.info("sendMessage: {}", message);

        session.send("/app/chat.sendMessage", message);
    }

    protected static abstract class FrameHandler<GameMessage extends Message> implements StompFrameHandler {

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return Message.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            Message message = (Message) payload;
            logger.debug("handleFrame: Received message: {} {}", message, headers);

            if (message.getType() == MessageType.GAME /* && message instanceof GameMessage */) {
                GameMessage gameMessage = (GameMessage) message;
                handleGame(gameMessage);
            } else {
                handleOther(message);
            }
        }

        protected void handleOther(Message message) {
            logger.debug("Ignoring other message: {}", message);
        }

        protected void handleGame(GameMessage message) {
            logger.debug("Ignoring game message: {}", message);
        }

    }

}
