package com.nter.test.secrethitler.legacy;

import org.json.JSONException;
import org.json.JSONObject;
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
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class SHClient {

    private static Logger logger = LoggerFactory.getLogger(SHClient.class);

    private final static WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
    private String username;
    private StompSession stompSession;

    public ListenableFuture<StompSession> connect() {

        Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
        List<Transport> transports = Collections.singletonList(webSocketTransport);

        SockJsClient sockJsClient = new SockJsClient(transports);
        sockJsClient.setMessageCodec(new Jackson2SockJsMessageCodec());

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

        //String url = "wss://api.project-g.xyz:443/ws";
        String url = "ws://localhost:8080/ws";
        return stompClient.connect(url, headers, new MyHandler());
    }

    public void subscribeChannels(StompSession session) throws ExecutionException, InterruptedException {
        this.stompSession = session;
        stompSession.subscribe("/topic/public", new StompFrameHandler() {

            public Type getPayloadType(StompHeaders stompHeaders) {
                return byte[].class;
            }

            public void handleFrame(StompHeaders stompHeaders, Object o) {
                String message = new String((byte[]) o);
                JSONObject obj = null;
                try {
                    obj = new JSONObject(message);
                    String output = obj.getString("content");


                    //JSONObject jsonMessage = new JSONObject(message);

                    logger.info("Received message " + message);
                    String content = getContent(message);


                    if (obj.getString("type").equals("START")) {
                        logger.info("Game started");
                        return;
                    } else if (obj.getString("type").equals("GAME")) {

                        logger.info("GameType: " + obj.getString("gameType"));
                        if (obj.getString("gameType").equals("FACTION")) {
                            if (message.contains("FASCIST"))
                                logger.info("You are FASCIST");
                            if (message.contains("LIBERAL"))
                                logger.info("You are LIBERAL");
                            if (message.contains("HITLER"))
                                logger.info("You are HITLER");
                        } else if (obj.getString("gameType").equals("VOTE")) {
                            chooseOne(content, "VOTE");
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

        stompSession.subscribe("/user/topic/public", new StompFrameHandler() {

            public Type getPayloadType(StompHeaders stompHeaders) {
                return byte[].class;
            }

            public void handleFrame(StompHeaders stompHeaders, Object o) {
                String message = new String((byte[]) o);
                JSONObject obj = null;
                try {
                    obj = new JSONObject(message);
                    String output = obj.getString("content");
                    //JSONObject jsonMessage = new JSONObject(message);

                    logger.info("Received message " + message);
                    String content = getContent(message);
                    if (obj.getString("type").equals("GAME")) {

                        if (obj.getString("gameType").equals("FACTION")) {
                            if (message.contains("FASCIST"))
                                logger.info("You are FASCIST");
                            if (message.contains("LIBERAL"))
                                logger.info("You are LIBERAL");
                            if (message.contains("HITLER"))
                                logger.info("You are HITLER");
                        } else if (obj.getString("gameType").equals("QUERY_CHANCELLOR")) {
                            chooseOne(content, "QUERY_CHANCELLOR");
                        } else if (obj.getString("gameType").equals("POLICIES")) {
                            chooseTwo(content, "POLICIES");
                        } else if (obj.getString("gameType").equals("POLICY")) {
                            chooseOne(content, "POLICY");
                        } else if (obj.getString("gameType").equals("TOP_POLICIES")) {
                            logger.info("Peeked policy: " + content);
                        } else if (obj.getString("gameType").equals("KILL")) {
                            chooseOne(content, "KILL");
                        } else if (obj.getString("gameType").equals("INVESTIGATE")) {
                            chooseOne(content, "INVESTIGATE");
                        } else if (obj.getString("gameType").equals("INVESTIGATE_RESULT")) {
                            logger.info("Investigation result: " + content);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private String getContent(String message) throws JSONException {
        JSONObject obj = new JSONObject(message);
        String output = obj.getString("content");
        logger.info("Message trimmed: " + output);
        return output;
    }


    private void chooseOne(String message, String type) throws JSONException {
        String userList = message.replace("Selectable:", "");
        String splitted[] = userList.split(",");

        int rnd = new Random().nextInt(splitted.length);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("Reply: " + splitted[rnd]);
        sendMessage(splitted[rnd], type);
    }

    private void chooseTwo(String message, String type) throws JSONException {
        String userList = message.replace("Selectable:", "");
        String splitted[] = userList.split(",");

        int rnd = new Random().nextInt(splitted.length);
        String answer = "";
        for (int i = 0; i < splitted.length; i++) {
            if (i != rnd)
                answer += splitted[i] + ",";
        }
        answer = answer.substring(0, answer.length() - 1);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("Reply: " + answer);
        sendMessage(answer, type);
    }

    public void sendMessage(String s, String type) throws JSONException {
        JSONObject join = new JSONObject();
        join.put("sender", username);
        join.put("type", "GAME");
        join.put("gameType", type);
        join.put("content", s);

        stompSession.send("/app/chat.sendMessage", join.toString().getBytes());
    }

    public void sendJoin(StompSession stompSession, String username) throws JSONException {
        JSONObject join = new JSONObject();
        join.put("sender", username);
        join.put("type", "JOIN");
        this.username = username;

        stompSession.send("/app/chat.addUser", join.toString().getBytes());
    }

    public void sendStart(StompSession stompSession, String username) throws JSONException {
        JSONObject join = new JSONObject();
        join.put("sender", username);
        join.put("type", "START");

        stompSession.send("/app/chat.sendMessage", join.toString().getBytes());
    }

    private class MyHandler extends StompSessionHandlerAdapter {
        public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
            logger.info("Connected");
        }
    }

    public static void main(String[] args) throws Exception {

        //Create clients
        logger.info("Creating clients");
        SHClient SHClient1 = new SHClient();
        SHClient SHClient2 = new SHClient();
        SHClient SHClient3 = new SHClient();
        SHClient SHClient4 = new SHClient();
        SHClient SHClient5 = new SHClient();

        logger.info("Connecting clients");
        ListenableFuture<StompSession> f1 = SHClient1.connect();
        ListenableFuture<StompSession> f2 = SHClient1.connect();
        ListenableFuture<StompSession> f3 = SHClient1.connect();
        ListenableFuture<StompSession> f4 = SHClient1.connect();
        ListenableFuture<StompSession> f5 = SHClient1.connect();

        logger.info("Creating sessions");
        StompSession stompSession1 = f1.get();
        StompSession stompSession2 = f2.get();
        StompSession stompSession3 = f3.get();
        StompSession stompSession4 = f4.get();
        StompSession stompSession5 = f5.get();

        logger.info("Subscribing to topic using sessions");
        SHClient1.subscribeChannels(stompSession1);
        SHClient2.subscribeChannels(stompSession2);
        SHClient3.subscribeChannels(stompSession3);
        SHClient4.subscribeChannels(stompSession4);
        SHClient5.subscribeChannels(stompSession5);

        logger.info("Sending join messages");
        int clientTimer = 100;
        SHClient1.sendJoin(stompSession1, "Cseni");
        Thread.sleep(clientTimer);
        SHClient2.sendJoin(stompSession2, "Linda");
        Thread.sleep(clientTimer);
        SHClient3.sendJoin(stompSession3, "Sali");
        Thread.sleep(clientTimer);
        SHClient4.sendJoin(stompSession4, "Rudolf");
        Thread.sleep(clientTimer);
        SHClient5.sendJoin(stompSession5, "Tamás");

        Thread.sleep(1000);
        logger.info("Starting game");
        SHClient5.sendStart(stompSession5, "Tamás");

        int sleepTimer = 600000;
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
