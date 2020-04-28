package com.nter.projectg.lobby;

import com.nter.projectg.games.common.GameHandler;
import com.nter.projectg.games.common.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LobbyHandler {

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    private final Map<String, Lobby> lobbies = new ConcurrentHashMap<>();
    private final GameHandler gameFactory = new GameHandler();
    private final Random rand = new Random();

    @Autowired
    private Constants constants;

    public Lobby createLobby(String gameName) {
        String name = gameName + "-" + (rand.nextInt(999) + 100);
        Lobby lobby = new Lobby(name, messagingTemplate);
        lobbies.put(name, lobby);

        return lobby;
    }


    public void remove(String user, String session) {
        findLobbyForUser(user).remove(user, session);
    }

    public Lobby findLobbyForUser(String user) {
        for (Lobby lobby : lobbies.values()) {
            if (lobby.getUsers().contains(user)) {
                return lobby;
            }
        }

        return null;
    }

    public Lobby findLobbyByName(String lobbyName) {
        for (Lobby lobby : lobbies.values()) {
            if (lobby.getName().equals(lobbyName)) {
                return lobby;
            }
        }

        return null;
    }

    public void add(String user, String session, String lobby) {
        findLobbyByName(lobby).add(user, session);
    }

    public List<String> getLobbies() {
        return new ArrayList<>(lobbies.keySet());
    }
}
