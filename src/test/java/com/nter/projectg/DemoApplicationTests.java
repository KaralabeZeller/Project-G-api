package com.nter.projectg;

import com.nter.test.secrethitler.SecretHitlerClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DemoApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(DemoApplicationTests.class);

    private static final long sleepMillis = TimeUnit.SECONDS.toMillis(1);

    @LocalServerPort
    private int port;

    private SecretHitlerClient client1;
    private SecretHitlerClient client2;
    private SecretHitlerClient client3;
    private SecretHitlerClient client4;
    private SecretHitlerClient client5;

    @Before
    public void setUp() throws Exception {
        logger.info("Creating clients");
        client1 = new SecretHitlerClient(port);
        client2 = new SecretHitlerClient(port);
        client3 = new SecretHitlerClient(port);
        client4 = new SecretHitlerClient(port);
        client5 = new SecretHitlerClient(port);

        logger.info("Connecting clients and opening sessions");
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
    }

    @After
    public void tearDown() {
        logger.info("Unsubscribing from topics");
        client1.unsubscribe();
        client2.unsubscribe();
        client3.unsubscribe();
        client4.unsubscribe();
        client5.unsubscribe();

        logger.info("Closing sessions and disconnecting clients");
        client1.disconnect();
        client2.disconnect();
        client3.disconnect();
        client4.disconnect();
        client5.disconnect();
    }

    @Test
    public void testJoin() throws InterruptedException {
        logger.info("Heló!");
        List<String> users = Arrays.asList("TESTER1", "TESTER2", "TESTER3", "TESTER4", "TESTER5");

        logger.info("Sending join messages: {}", users);
        client1.sendJoin("TESTER1");
        client2.sendJoin("TESTER2");
        client3.sendJoin("TESTER3");
        client4.sendJoin("TESTER4");
        client5.sendJoin("TESTER5");

        sleep();

        Set<String> expectedJoin = new HashSet<>(users);
        logger.info("Expecting join messages: {}", users);
        Assert.assertEquals(expectedJoin, client1.expectJoin());
        Assert.assertEquals(expectedJoin, client2.expectJoin());
        Assert.assertEquals(expectedJoin, client3.expectJoin());
        Assert.assertEquals(expectedJoin, client4.expectJoin());
        Assert.assertEquals(expectedJoin, client5.expectJoin());

        logger.info("Akkor Heló!");
    }

    // TODO avoid sleeping in unit and integration tests
    private void sleep() throws InterruptedException {
        logger.debug("Sleeping for: {} milliseconds", sleepMillis);
        Thread.sleep(sleepMillis);
    }

}
