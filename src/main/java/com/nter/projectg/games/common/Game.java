package com.nter.projectg.games.common;


public class Game implements Runnable{
	
	private String name;
	protected int playerCount = 0;
	protected int minPlayers = 0;
	protected int maxPlayers = 0;
	protected volatile boolean isRunning = false;
	
	public Game(String name, int minPlayer, int maxPlayer) {
		this.name = name;
		this.minPlayers = minPlayer;
		this.maxPlayers = maxPlayer;
		
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
	

}


