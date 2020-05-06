package com.nter.projectg.lobby;

import com.nter.projectg.games.common.Game;
import com.nter.projectg.games.common.GameHandler;
import com.nter.projectg.games.common.util.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class LobbyHandler {

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    GameHandler gameFactory;

    private final Map<String, Lobby> lobbies = new ConcurrentHashMap<>();
    private final Random rand = new Random();
    private Timer timer;

    public List<String> getLobbies() {
        return new ArrayList<>(lobbies.keySet());
    }

    private Lobby findLobbyByName(String lobbyName) {
        return lobbies.get(lobbyName);
    }

    public void createLobby(String gameName) {
        String name = gameName + "-" + (rand.nextInt(999) + 100);
        Lobby lobby = new Lobby(name, messagingTemplate);
        lobbies.put(name, lobby);
        this.timer = new Timer();
    }

    private void closeLobby(String name) {

        lobbies.remove(name);
        Game game = gameFactory.get(name);
        game.stop();
        gameFactory.remove(name);

    }

    public Lobby get(String lobby) {
        return lobbies.get(lobby);
    }

    public Optional<Lobby> findLobbyForUser(String user) {
        return lobbies.values().stream().filter(l -> l.getUsers().contains(user)).findFirst();
    }

    public void add(String lobby, String user, String session) {
        findLobbyByName(lobby).add(user, session);
    }

    public void remove(String user, String session) {
        findLobbyForUser(user).ifPresent(lobby -> {
            lobby.remove(user, session);
            if (lobby.getUsers().isEmpty()) {
                timer.delay(() -> closeLobby(lobby.getName()), 240);
            }
        });
    }

}
