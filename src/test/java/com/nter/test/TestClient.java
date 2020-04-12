package com.nter.test;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketHttpHeaders;
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

public class TestClient {

    private static Logger logger = LoggerFactory.getLogger(TestClient.class);
    private int port;

    private final static WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

    public TestClient(int port) {
        this.port = port;
    }

    public ListenableFuture<StompSession> connect() {

        Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
        List<Transport> transports = Collections.singletonList(webSocketTransport);

        SockJsClient sockJsClient = new SockJsClient(transports);
        sockJsClient.setMessageCodec(new Jackson2SockJsMessageCodec());

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

        //String url = "wss://api.project-g.xyz:443/ws";
        String url = "ws://localhost:" + port + "/ws";
        return stompClient.connect(url, headers, new MyHandler());
    }

    public void subscribeChannels(StompSession stompSession) throws ExecutionException, InterruptedException {
        stompSession.subscribe("/topic/public", new StompFrameHandler() {

            public Type getPayloadType(StompHeaders stompHeaders) {
                return byte[].class;
            }

            public void handleFrame(StompHeaders stompHeaders, Object o) {
                logger.info("Received message " + new String((byte[]) o));
            }
        });

        stompSession.subscribe("/user/topic/public", new StompFrameHandler() {

            public Type getPayloadType(StompHeaders stompHeaders) {
                return byte[].class;
            }

            public void handleFrame(StompHeaders stompHeaders, Object o) {
                String message = new String((byte[]) o);
                //JSONObject jsonMessage = new JSONObject(message);

                logger.info("Received message " + message);

                if(message.contains("\"type\":\"START\"")) {
                    logger.info("Game started" );
                } else if(message.contains("\"gameMessageType\":\"FACTION\"")) {
                    if(message.contains("FASCIST"))
                        logger.info("You are FASCIST");
                    if(message.contains("LIBERAL"))
                        logger.info("You are LIBERAL");
                    if(message.contains("HITLER"))
                        logger.info("You are HITLER");
                }
            }
        });
    }

    public void sendJoin(StompSession stompSession, String username) {
        JsonObject join = new JsonObject();
        join.addProperty("sender", username);
        join.addProperty("type", "JOIN");

        stompSession.send("/app/chat.addUser", join.toString().getBytes());
    }

    private class MyHandler extends StompSessionHandlerAdapter {
        public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
            logger.info("Connected");
        }
    }

}
