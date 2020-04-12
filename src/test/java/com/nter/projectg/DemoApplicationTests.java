package com.nter.projectg;

import com.nter.test.TestClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DemoApplicationTests {

	private static Logger logger = LoggerFactory.getLogger(DemoApplicationTests.class);
	@Value("${local.server.port}")
	private int port;

	@Test
	public void testClients() throws ExecutionException, InterruptedException {
		//Create clients
		logger.info("Creating clients");
		TestClient client1 = new TestClient(port);
		TestClient client2 = new TestClient(port);
		TestClient client3 = new TestClient(port);
		TestClient client4 = new TestClient(port);
		TestClient client5 = new TestClient(port);

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

		int sleepTimer = 1000;
		logger.info("Sleep for : " + sleepTimer);
		Thread.sleep(sleepTimer);

		logger.info("Disconnecting sessions");

		disconnectClient(stompSession1);
		disconnectClient(stompSession2);
		disconnectClient(stompSession3);
		disconnectClient(stompSession4);
		disconnectClient(stompSession5);

		logger.info("Akkor Heló!");


	}

	private void disconnectClient(StompSession stompSession1) {
		try {
			stompSession1.disconnect();
		}catch(IllegalStateException ex) {
			logger.warn("Illegal state exception ", ex);
		}
	}

}
