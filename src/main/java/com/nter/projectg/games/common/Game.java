package com.nter.projectg.games.common;

import com.nter.projectg.common.Lobby;
import com.nter.projectg.controller.GameClient;
import com.nter.projectg.games.secrethitler.SecretHitler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class Game<Client extends GameClient> implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SecretHitler.class);

    private final Lobby lobby;

    private final String name;
    private final int minPlayers;
    private final int maxPlayers;

    private final List<Client> clients = new ArrayList<>();

    private volatile boolean isRunning = false;

    public Game(Lobby lobby, String name, int minPlayer, int maxPlayer) {
        this.lobby = lobby;

        this.name = name;
        this.minPlayers = minPlayer;
        this.maxPlayers = maxPlayer;

        logger.debug("Initializing game: {} {}", name, lobby);
        createClients();
        logger.info("Initialized game: {} {}", name, lobby);
    }

    public String getName() {
        return name;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getPlayerCount() {
        return clients.size();
    }

    protected Lobby getLobby() {
        return lobby;
    }

    protected List<Client> getPlayers() {
        return clients;
    }

    protected abstract Client createClient(String name);

    private void createClients() {
        logger.debug("Creating clients: {}", clients);
        for (String user : lobby.getUsers()) {
            clients.add(createClient(user));
        }
        logger.info("Created clients: {}", clients);
    }

    @Override
    public void run() {
        isRunning = true;

        // TODO
        Thread.currentThread().setName(getName());
        System.out.println("Game starting: " + getName());

        System.out.println("Game closing: " + getName());
    }

    @Override
    public String toString() {
        return "Game{" +
                "lobby=" + lobby +
                ", name='" + name + '\'' +
                ", clients=" + clients +
                '}';
    }
}


