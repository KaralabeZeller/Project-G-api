package com.nter.projectg.games.common;

import com.nter.projectg.common.Lobby;
import com.nter.projectg.model.common.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class Game<GameMessage extends Message, GamePlayer extends Player<GameMessage>> implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Game.class);

    private final Lobby lobby;

    private final String name;
    private final int minPlayers;
    private final int maxPlayers;

    private final List<GamePlayer> players = new ArrayList<>();

    public Game(Lobby lobby, String name, int minPlayers, int maxPlayers) {
        this.lobby = lobby;

        this.name = name;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;

        logger.debug("Initializing game: {}", this);
        createPlayers();
        logger.info("Initialized game: {}", this);
    }

    public String getName() {
        return name;
    }

    protected List<GamePlayer> getPlayers() {
        return players;
    }

    private void createPlayers() {
        logger.debug("Creating players: {}", players);

        int userCount = lobby.getUsers().size();
        if (userCount < minPlayers || userCount >= maxPlayers) {
            logger.warn("Player count is not in the interval: {} < {} < {}", minPlayers, userCount, maxPlayers);
        }

        for (String user : lobby.getUsers()) {
            players.add(createPlayer(user));
        }

        logger.info("Created players: {}", players);
    }

    protected abstract GamePlayer createPlayer(String name);

    protected void sendToAll(GameMessage message) {
        lobby.sendToAll(message);
    }

    protected void sendToPlayer(String name, GameMessage message) {
        lobby.sendToUser(name, message);
    }

    protected Consumer<GameMessage> sendToAll() {
        return message -> sendToAll(message);
    }

    protected Consumer<GameMessage> sendToPlayer(String name) {
        return message -> sendToPlayer(name, message);
    }

    @Override
    public void run() {
        // TODO
    }

    @Override
    public String toString() {
        return "Game{" +
                "name='" + name + '\'' +
                ", players=" + players +
                ", lobby=" + lobby +
                '}';
    }
}


