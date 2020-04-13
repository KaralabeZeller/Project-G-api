package com.nter.projectg;

import com.nter.test.secrethitler.SecretHitlerClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DemoApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(DemoApplicationTests.class);

    @LocalServerPort
    private int port;

    @Test
    public void testClients() throws ExecutionException, InterruptedException {
        logger.info("Creating clients");
        SecretHitlerClient client1 = new SecretHitlerClient(port);
        SecretHitlerClient client2 = new SecretHitlerClient(port);
        SecretHitlerClient client3 = new SecretHitlerClient(port);
        SecretHitlerClient client4 = new SecretHitlerClient(port);
        SecretHitlerClient client5 = new SecretHitlerClient(port);

        logger.info("Connecting clients");
        ListenableFuture<StompSession> f1 = client1.connect();
        ListenableFuture<StompSession> f2 = client1.connect();
        ListenableFuture<StompSession> f3 = client1.connect();
        ListenableFuture<StompSession> f4 = client1.connect();
        ListenableFuture<StompSession> f5 = client1.connect();

        logger.info("Creating sessions");
        StompSession session1 = f1.get();
        StompSession session2 = f2.get();
        StompSession session3 = f3.get();
        StompSession session4 = f4.get();
        StompSession session5 = f5.get();

        logger.info("Subscribing to topic using sessions");
        client1.subscribe(session1);
        client2.subscribe(session2);
        client3.subscribe(session3);
        client4.subscribe(session4);
        client5.subscribe(session5);

        logger.info("Sending join messages");
        int clientTimer = 100;
        client1.sendJoin(session1, "TESTER1");
        Thread.sleep(clientTimer);
        client2.sendJoin(session2, "TESTER2");
        Thread.sleep(clientTimer);
        client3.sendJoin(session3, "TESTER3");
        Thread.sleep(clientTimer);
        client4.sendJoin(session4, "TESTER4");
        Thread.sleep(clientTimer);
        client5.sendJoin(session5, "TESTER5");

        int sleepTimer = 1000;
        logger.info("Sleep for: {} milliseconds", sleepTimer);
        Thread.sleep(sleepTimer);

        logger.info("Disconnecting sessions");
        disconnect(session1);
        disconnect(session2);
        disconnect(session3);
        disconnect(session4);
        disconnect(session5);

        logger.info("Akkor Heló!");
    }

    private void disconnect(StompSession session) {
        try {
            session.disconnect();
        } catch (IllegalStateException ex) {
            logger.warn("Failed to disconnect session", ex);
        }
    }

}
