package com.nter.projectg.games.secrethitler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.nter.projectg.controller.GameClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nter.projectg.common.Lobby;
import com.nter.projectg.controller.WebSocketEventListener;
import com.nter.projectg.games.common.Game;


public class SecretHitler extends Game<UserSecretHitler> {
	
	private static final Logger logger = LoggerFactory.getLogger(SecretHitler.class);

	private Assets assets;
	private int hitlerID;
	private int chancellorID;
	private int presidentID;


	
	public SecretHitler() {
		super("Secret Hitler", 5, 10);
		logger.info("Started secret Hitler");
		init();
		sendFactions();
	}

	private void init() {
		if(getPlayerCount() < minPlayers || getPlayerCount() >= maxPlayers)
			logger.info("Player count is not in the interval!");

		assets = new Assets(getClientList());
		assets.updateNotElect(presidentID, chancellorID);
		hitlerID = -1;
		chancellorID = -1;
		presidentID = -1;


	}


	private void sendFactions() {
		List<UserSecretHitler> players = new ArrayList<>(getClientList());

		Collections.shuffle(players);
		int index = 0;

		for (UserSecretHitler player : players) {
			Constants.Faction f = assets.getFactions().get(index);
			player.setFaction(f);
			if (f == Constants.Faction.HITLER)
				hitlerID = index;
			index++;
		}

		//sendHitlerToFascists();
		System.out.println("--Factions are dealt");

	}
	@Override
	public void run() {
		return;
	}

	@Override
	protected UserSecretHitler createClient(String user) {
		return new UserSecretHitler(user);
	}
}
