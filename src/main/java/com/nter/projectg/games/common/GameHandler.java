package com.nter.projectg.games.common;

import com.nter.projectg.games.common.util.Constants.GameName;
import com.nter.projectg.games.secrethitler.SecretHitlerGame;
import com.nter.projectg.lobby.Lobby;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameHandler {

    private final Map<String, Game<?, ?>> games = new ConcurrentHashMap<>();

    public Game<?, ?> createGame(GameName name, Lobby lobby) {
        Game<?, ?> game = doCreateGame(name, lobby);
        games.put(lobby.getName(), game);
        return game;
    }

    private Game<?, ?> doCreateGame(GameName name, Lobby lobby) {
        if (name == GameName.SECRET_HITLER) {
            return new SecretHitlerGame(lobby);
        }

        throw new UnsupportedOperationException("Unsupported game: " + name);
    }

    public Game<?, ?> get(String lobby) {
        return games.get(lobby);
    }

    public boolean exists(String name) {
        return Arrays.stream(GameName.values()).anyMatch(n -> n.name().equals(name));
    }

    public void remove(String lobbyID) {
        games.remove(lobbyID);
    }

}
