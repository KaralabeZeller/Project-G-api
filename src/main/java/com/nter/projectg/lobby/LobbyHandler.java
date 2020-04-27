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
    private GameHandler gameFactory = new GameHandler();
    private Random rand = new Random();

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
        Lobby returnLobby = null;

        for(Lobby lobby : lobbies.values()) {
            for(String us : lobby.getUsers()) {
                if(us.equals(user)) {
                    returnLobby = lobby;
                    break;
                }
            }
        }
        return returnLobby;
    }

    public Lobby findLobbyByName(String lobbyName) {
        Lobby returnLobby = null;

        for(Lobby lobby : lobbies.values()) {
            if(lobby.getName().equals(lobbyName)) return lobby;
        }
        return returnLobby;
    }

    public void add(String user, String session, String lobby) {
        findLobbyByName(lobby).add(user, session);
    }

    public List<String> getLobbies() {
        List<String> returnList = new ArrayList<>();
        returnList.addAll(lobbies.keySet());
        return returnList;
    }
}
