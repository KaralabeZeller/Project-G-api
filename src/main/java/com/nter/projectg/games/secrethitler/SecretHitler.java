package com.nter.projectg.games.secrethitler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nter.projectg.common.Lobby;
import com.nter.projectg.controller.WebSocketEventListener;
import com.nter.projectg.games.common.Game;


public class SecretHitler extends Game {
	
	private static final Logger logger = LoggerFactory.getLogger(SecretHitler.class);
	
	public SecretHitler() {
		super("Secret Hitler", 5, 10);
		logger.info("Started secret Hitler");
	}

	@Override
	public void run() {
	
		return;
	}

	
}
