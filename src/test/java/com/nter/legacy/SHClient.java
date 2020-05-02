package com.nter.legacy;

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
import java.util.*;

// TODO eliminate JSONObject and String
public class SHClient {

    private static final Logger logger = LoggerFactory.getLogger(SHClient.class);

    private final static WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
    private final Random random = new Random();
    private String username;
    private StompSession stompSession;
    private final String lobbyName = "SECRET_HITLER-716";

    public ListenableFuture<StompSession> connect() {

        Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
        List<Transport> transports = Collections.singletonList(webSocketTransport);

        SockJsClient sockJsClient = new SockJsClient(transports);
        sockJsClient.setMessageCodec(new Jackson2SockJsMessageCodec());

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

        //String url = "wss://api.project-g.xyz:443/ws";
        String url = "ws://localhost:8080/ws";
        return stompClient.connect(url, headers, new SessionHandler());
    }

    public void subscribeChannels(StompSession session) {
        this.stompSession = session;

        stompSession.subscribe("/topic/lobby/" + lobbyName, new StompFrameHandler() {

            public Type getPayloadType(StompHeaders stompHeaders) {
                return byte[].class;
            }

            public void handleFrame(StompHeaders stompHeaders, Object o) {
                try {
                    JSONObject message = new JSONObject(new String((byte[]) o));
                    logger.info("Received lobby message: {} {}", message, stompHeaders);

                    String type = message.getString("type");
                    if (type.equals("LOBBY")) {
                        // ignore
                    } else {
                        logger.warn("Unexpected lobby message: {}", new String((byte[]) o));
                    }
                } catch (Exception e) {
                    logger.error("Failed to handle message", e);
                }
            }
        });

        stompSession.subscribe("/topic/game/" + lobbyName, new StompFrameHandler() {

            public Type getPayloadType(StompHeaders stompHeaders) {
                return byte[].class;
            }

            public void handleFrame(StompHeaders stompHeaders, Object o) {
                try {
                    JSONObject message = new JSONObject(new String((byte[]) o));
                    logger.info("Received game message: {} {}", message, stompHeaders);

                    String type = message.getString("type");
                    if (type.equals("START")) {
                        String content = message.getString("content");
                        logger.info("Game started: {}", content);
                    } else if (type.equals("GAME")) {

                        String content = message.getString("content");
                        String gameType = message.getString("gameType");
                        if (gameType.equals("PRESIDENT") || gameType.equals("CHANCELLOR") ||
                                gameType.equals("VOTED") || gameType.equals("ENACTED_POLICY") ||
                                gameType.equals("KILLED") || gameType.equals("STATE")) {
                            // ignore
                        } else if (gameType.equals("VOTE")) {
                            chooseOne(content, "VOTE");
                        } else {
                            logger.warn("Unexpected game message: {}", new String((byte[]) o));
                        }

                    } else {
                        logger.warn("Unexpected other message: {}", new String((byte[]) o));
                    }
                } catch (Exception e) {
                    logger.error("Failed to handle message", e);
                }
            }
        });

        stompSession.subscribe("/user/topic/game/" + lobbyName, new StompFrameHandler() {

            public Type getPayloadType(StompHeaders stompHeaders) {
                return byte[].class;
            }

            public void handleFrame(StompHeaders stompHeaders, Object o) {
                try {
                    JSONObject message = new JSONObject(new String((byte[]) o));
                    logger.info("Received user game message: {} {}", message, stompHeaders);

                    String type = message.getString("type");
                    if (type.equals("GAME")) {

                        String content = message.getString("content");
                        String gameType = message.getString("gameType");
                        if (gameType.equals("FACTION") || gameType.equals("HITLER") || gameType.equals("FELLOW_FASCIST")) {
                            // ignore
                        } else if (gameType.equals("VOTE")) {
                            chooseOne(content, "VOTE");
                        } else if (gameType.equals("QUERY_CHANCELLOR")) {
                            chooseOne(content, "QUERY_CHANCELLOR");
                        } else if (gameType.equals("POLICIES")) {
                            chooseTwo(content, "POLICIES");
                        } else if (gameType.equals("POLICY")) {
                            chooseOne(content, "POLICY");
                        } else if (gameType.equals("TOP_POLICIES")) {
                            logger.info("Peeked policy: {}", content);
                        } else if (gameType.equals("KILL")) {
                            chooseOne(content, "KILL");
                        } else if (gameType.equals("KILLED")) {
                            logger.info("Received killed message: {}", content);
                        } else if (gameType.equals("INVESTIGATE")) {
                            chooseOne(content, "INVESTIGATE");
                        } else if (gameType.equals("INVESTIGATE_RESULT")) {
                            logger.info("Investigation result: {}", content);
                        } else {
                            logger.warn("Unexpected user game message: {}", new String((byte[]) o));
                        }

                    } else {
                        logger.warn("Unexpected other message: {}", new String((byte[]) o));
                    }

                } catch (Exception e) {
                    logger.error("Failed to handle message", e);
                }
            }
        });
    }

    private void chooseOne(String message, String type) throws JSONException {
        String[] split = message.split(",");

        String result = split[random.nextInt(split.length)];

        logger.info("chooseOne: {} from {}", result, split);
        sendGame(type, result);
    }

    private void chooseTwo(String message, String type) throws JSONException {
        String[] split = message.split(",");

        // copy to avoid UnsupportedOperationException
        List<String> result = new ArrayList<>(Arrays.asList(split));
        result.remove(random.nextInt(split.length));

        logger.info("chooseTwo: {} from {}", result, split);
        sendGame(type, String.join(",", result));
    }

    public void join(String username) throws JSONException {
        this.username = username;
        sendLobby("JOIN", null);
    }

    public void sendLobby(String type, String content) throws JSONException {
        JSONObject join = new JSONObject();
        join.put("type", "LOBBY");
        join.put("lobbyType", type);
        join.put("sender", username);
        join.put("lobby", lobbyName);
        join.put("content", content);

        stompSession.send("/app/lobby/" + lobbyName, join.toString().getBytes());
    }

    private void sendStart(String content) throws JSONException {
        JSONObject start = new JSONObject();
        start.put("type", "START");
        start.put("sender", username);
        start.put("lobby", lobbyName);
        start.put("content", content);

        stompSession.send("/app/game/" + lobbyName, start.toString().getBytes());
    }

    public void sendGame(String type, String content) throws JSONException {
        JSONObject game = new JSONObject();
        game.put("type", "GAME");
        game.put("sender", username);
        game.put("gameType", type);
        game.put("content", content);
        game.put("lobby", lobbyName);

        stompSession.send("/app/game/" + lobbyName, game.toString().getBytes());
    }

    private static class SessionHandler extends StompSessionHandlerAdapter {
        public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
            logger.info("Connected: {} {}", stompSession, stompHeaders);
        }
    }

    public static void main(String[] args) throws Exception {
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
        SHClient1.join("Cseni");
        SHClient2.join("Linda");
        SHClient3.join("Sali");
        SHClient4.join("Rudolf");
        SHClient5.join("Tamás");

        sleep(4500);

        logger.info("Starting game");
        SHClient5.sendStart("SecretHitler");

        sleep(900000);

        logger.info("Disconnecting sessions");
        stompSession1.disconnect();
        stompSession2.disconnect();
        stompSession3.disconnect();
        stompSession4.disconnect();
        stompSession5.disconnect();
        logger.info("Akkor Heló!");
    }

    // TODO avoid sleeping in unit and integration tests
    private static void sleep(long sleepMillis) throws InterruptedException {
        logger.debug("Sleeping for: {} milliseconds", sleepMillis);
        Thread.sleep(sleepMillis);
    }

}
