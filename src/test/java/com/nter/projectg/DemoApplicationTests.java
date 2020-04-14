package com.nter.projectg;

import com.nter.test.secrethitler.SecretHitlerClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DemoApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(DemoApplicationTests.class);

    @LocalServerPort
    private int port;

    private final long sleepTimer = TimeUnit.SECONDS.toMillis(1);

    @Test
    public void testClients() throws ExecutionException, InterruptedException {
        logger.info("Creating clients");
        SecretHitlerClient client1 = new SecretHitlerClient(port);
        SecretHitlerClient client2 = new SecretHitlerClient(port);
        SecretHitlerClient client3 = new SecretHitlerClient(port);
        SecretHitlerClient client4 = new SecretHitlerClient(port);
        SecretHitlerClient client5 = new SecretHitlerClient(port);

        logger.info("Connecting clients and creating sessions");
        client1.connect();
        client2.connect();
        client3.connect();
        client4.connect();
        client5.connect();

        logger.info("Subscribing to topics");
        client1.subscribe();
        client2.subscribe();
        client3.subscribe();
        client4.subscribe();
        client5.subscribe();

        logger.info("Sending join messages");
        client1.sendJoin("TESTER1");
        client2.sendJoin("TESTER2");
        client3.sendJoin("TESTER3");
        client4.sendJoin("TESTER4");
        client5.sendJoin("TESTER5");

        // TODO avoid sleeping in tests
        logger.info("Sleeping for: {} milliseconds", sleepTimer);
        Thread.sleep(sleepTimer);

        logger.info("Expecting join messages");
        Set<String> expectedJoin = new HashSet<>(Arrays.asList("TESTER1", "TESTER2", "TESTER3", "TESTER4", "TESTER5"));
        Assert.assertEquals(expectedJoin, client1.expectJoin());
        Assert.assertEquals(expectedJoin, client2.expectJoin());
        Assert.assertEquals(expectedJoin, client3.expectJoin());
        Assert.assertEquals(expectedJoin, client4.expectJoin());
        Assert.assertEquals(expectedJoin, client5.expectJoin());

        // TODO avoid sleeping in tests
        logger.info("Sleeping for: {} milliseconds", sleepTimer);
        Thread.sleep(sleepTimer);

        logger.info("Unsubscribing from topics");
        client1.unsubscribe();
        client2.unsubscribe();
        client3.unsubscribe();
        client4.unsubscribe();
        client5.unsubscribe();

        logger.info("Disconnecting sessions and clients");
        client1.disconnect();
        client2.disconnect();
        client3.disconnect();
        client4.disconnect();
        client5.disconnect();

        logger.info("Akkor Heló!");
    }

}
