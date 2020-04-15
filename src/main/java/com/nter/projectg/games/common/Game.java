package com.nter.projectg.games.common;

import com.nter.projectg.common.Lobby;
import com.nter.projectg.model.common.Message;
import com.nter.projectg.model.common.Message.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

public abstract class Game<GameMessage extends Message, GamePlayer extends Player<GameMessage>> {

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
        initializePlayers();
        logger.info("Initialized game: {}", this);
    }

    public String getName() {
        return name;
    }

    protected List<GamePlayer> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    protected GamePlayer findPlayer(String name) {
        for (GamePlayer player : players) {
            if (Objects.equals(player.getName(), name)) {
                return player;
            }
        }

        throw new IllegalArgumentException(String.format("Failed to find player: Invalid name: %s", name));
    }

    private void initializePlayers() {
        logger.debug("Initializing players: {}", players);

        Collection<String> users = lobby.getUsers();
        if (users.size() < minPlayers || users.size() >= maxPlayers) {
            logger.warn("Player count is not in the interval: {} < {} < {}", minPlayers, users.size(), maxPlayers);
            // TODO ignore invalid state
        }

        for (String user : users) {
            players.add(createPlayer(user));
        }

        logger.info("Initialized players: {}", players);
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

    public void handle(Message message) {
        if (message.getType() == MessageType.GAME /* && message instanceof GameMessage */) {
            logger.debug("Handling message: {}", message);
            GameMessage gameMessage = (GameMessage) message;
            handleGame(gameMessage);
        } else {
            logger.debug("Ignoring message: {}", message);
        }
    }

    protected abstract void handleGame(GameMessage message);

    public void start(String user) {
        GamePlayer player = findPlayer(user);

        // Broadcast start message to all sessions
        Message message = new Message();
        message.setType(MessageType.START);
        message.setSender(player.getName());
        message.setContent(name);
        lobby.sendToAll(message);
    }

    // TODO implement restore state / messages on reconnect
    public void reconnect(String user) {
        GamePlayer player = findPlayer(user);

        // Send start message to all sessions
        Message message = new Message();
        message.setType(MessageType.START);
        message.setSender(player.getName());
        message.setContent(name);
        lobby.sendToUser(player.getName(), message);
    }

    public void stop() {
        // TODO implement
    }

    // TODO maybe remove lobby
    @Override
    public String toString() {
        return "Game{" +
                "name='" + name + '\'' +
                ", players=" + players +
                ", lobby=" + lobby +
                '}';
    }
}
