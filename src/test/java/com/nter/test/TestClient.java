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

    private final static WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

    public ListenableFuture<StompSession> connect() {

        Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
        List<Transport> transports = Collections.singletonList(webSocketTransport);

        SockJsClient sockJsClient = new SockJsClient(transports);
        sockJsClient.setMessageCodec(new Jackson2SockJsMessageCodec());

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

        String url = "wss://api.project-g.xyz:443/ws";
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

    public static void main(String[] args) throws Exception {

        //Create clients
        logger.info("Creating clients");
        TestClient client1 = new TestClient();
        TestClient client2 = new TestClient();
        TestClient client3 = new TestClient();
        TestClient client4 = new TestClient();
        TestClient client5 = new TestClient();

        logger.info("Connecting clients");
        ListenableFuture<StompSession> f1 = client1.connect();
        ListenableFuture<StompSession> f2 = client1.connect();
        ListenableFuture<StompSession> f3 = client1.connect();
        ListenableFuture<StompSession> f4 = client1.connect();
        ListenableFuture<StompSession> f5 = client1.connect();

        logger.info("Creating sessions");
        StompSession stompSession1 = f1.get();
        StompSession stompSession2 = f2.get();
        StompSession stompSession3 = f3.get();
        StompSession stompSession4 = f4.get();
        StompSession stompSession5 = f5.get();

        logger.info("Subscribing to topic using sessions");
        client1.subscribeChannels(stompSession1);
        client2.subscribeChannels(stompSession2);
        client3.subscribeChannels(stompSession3);
        client4.subscribeChannels(stompSession4);
        client5.subscribeChannels(stompSession5);

        logger.info("Sending join messages");
        int clientTimer = 100;
        client1.sendJoin(stompSession1, "TESTER1");
        Thread.sleep(clientTimer);
        client2.sendJoin(stompSession2, "TESTER2");
        Thread.sleep(clientTimer);
        client3.sendJoin(stompSession3, "TESTER3");
        Thread.sleep(clientTimer);
        client4.sendJoin(stompSession4, "TESTER4");
        Thread.sleep(clientTimer);
        client5.sendJoin(stompSession5, "TESTER5");

        int sleepTimer = 60000;
        logger.info("Sleep for : " + sleepTimer);
        Thread.sleep(sleepTimer);

        logger.info("Disconnecting sessions");
        stompSession1.disconnect();
        stompSession2.disconnect();
        stompSession3.disconnect();
        stompSession4.disconnect();
        stompSession5.disconnect();
        logger.info("Akkor Heló!");
    }

}
