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
    private final String lobbyName = "SECRET_HITLER-396";

    private StompSession session;
    private StompSession.Subscription subscriptionLobby;
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
        logger.debug("Subscribing: {} {} {}", subscriptionLobby, subscriptionPublic, subscriptionUser);

        subscriptionLobby = session.subscribe("/topic/lobby/" + lobbyName, createHandlerLobby());
        subscriptionPublic = session.subscribe("/topic/game", createHandlerPublic());
        subscriptionUser = session.subscribe("/user/topic/game", createHandlerUser());

        logger.info("Subscribed: {} {} {}", subscriptionLobby, subscriptionPublic, subscriptionUser);
    }

    public void unsubscribe() {
        logger.debug("Unsubscribing: {} {} {}", subscriptionLobby, subscriptionPublic, subscriptionUser);

        subscriptionLobby.unsubscribe();
        subscriptionPublic.unsubscribe();
        subscriptionUser.unsubscribe();

        logger.info("Unsubscribed: {} {} {}", subscriptionLobby, subscriptionPublic, subscriptionUser);
    }

    protected abstract FrameHandler<Message> createHandlerLobby();

    protected abstract FrameHandler<GameMessage> createHandlerPublic();

    protected abstract FrameHandler<GameMessage> createHandlerUser();

    protected void sendLobby(Message message) {
        logger.info("sendLobby: {}", message);

        session.send("/app/lobby/" + lobbyName, message);
    }

    protected void sendGame(Message message) {
        logger.info("sendGame: {}", message);

        session.send("/app/game", message);
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
