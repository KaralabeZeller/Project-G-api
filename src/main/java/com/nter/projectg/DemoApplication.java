package com.nter.projectg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nter.projectg.common.Lobby;
import com.nter.projectg.controller.WebSocketEventListener;
import com.nter.projectg.games.common.Game;
import com.nter.projectg.games.secrethitler.SecretHitler;

@SpringBootApplication
@RestController
public class DemoApplication {
	private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
		
		logger.info("Creating game Secreet  Hitler");
		Lobby lobby = new Lobby();
		
	}
}