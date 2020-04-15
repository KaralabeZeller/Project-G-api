package com.nter.test.common;

import com.nter.projectg.model.common.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
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

// TODO extract generic functionality into Client from SecretHitlerClient
public abstract class Client {

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

    protected abstract FrameHandler createHandlerPublic();

    protected abstract FrameHandler createHandlerUser();

    // TODO refactor
    protected void sendAddUser(Message message) {
        logger.info("sendAddUser: {}", message);

        session.send("/app/chat.addUser", message);
    }

    // TODO refactor
    protected void sendMessage(Message message) {
        logger.info("sendMessage: {}", message);

        session.send("/app/chat.sendMessage", message);
    }

    protected static class SessionHandler extends StompSessionHandlerAdapter {

        @Override
        public void afterConnected(StompSession session, StompHeaders headers) {
            logger.debug("Connected in session: {} {}", session, headers);
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            logger.warn("Exception in session: {} {} {} {}", session, command, headers, exception);
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            logger.warn("Error in session: {} {}", session, exception);
        }

    }

    protected static abstract class FrameHandler implements StompFrameHandler {

        private volatile Message message;

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return Message.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            Message message = (Message) payload;
            logger.debug("Received message: {} {}", message, headers);

            this.message = message;
            handleMessage(message);
        }

        public Message peekMessage() {
            return message;
        }

        protected abstract void handleMessage(Message message);

    }

}
