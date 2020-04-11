package com.nter.projectg.games.common;


import com.nter.projectg.common.Lobby;
import com.nter.projectg.controller.GameClient;

import java.util.List;

public abstract class Game<Client extends GameClient> implements Runnable{
	
	private String name;
	protected int playerCount = 0;
	protected int minPlayers = 0;
	protected int maxPlayers = 0;
	protected volatile boolean isRunning = false;

	private List<Client> clientList;
	
	public Game(String name, int minPlayer, int maxPlayer) {
		this.name = name;
		this.minPlayers = minPlayer;
		this.maxPlayers = maxPlayer;
		createClients();
		
	}

	public String getName() {
		return name;
	}
	
	public int getMinPlayer() {
		return minPlayers;
	}
	
	public int getMaxPlayer() {
		return maxPlayers;
	}
	
	public int getPlayerCount() {
		return playerCount;
	}
	
	public void setPlayerCount(int playerCount) {
		this.playerCount = playerCount;
	}

	@Override
	public void run() {
		isRunning = true;
		Thread.currentThread().setName(getName());
		System.out.println("Game starting: " + getName());
		
		System.out.println("Game closing: " + getName());
		return;
		
	}

	private void createClients() {
		for (String user : Lobby.getUsers()) {
			clientList.add(createClient(user));
		}
	}

	protected abstract Client createClient(String user);

	protected int getUserCount(){
		return clientList.size();
	}

	protected List<Client> getClientList() {
		return clientList;
	}


}


