package com.nter.projectg.games.common;

import com.nter.projectg.games.common.util.Constants;
import com.nter.projectg.games.secrethitler.SecretHitlerGame;
import com.nter.projectg.lobby.Lobby;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameHandler {

    private final Map<String, Game<?, ?>> games = new ConcurrentHashMap<>();

    public Game<?, ?> createGame(Constants.GAME_NAME name, Lobby lobby) {
        Game<?, ?> game = doCreateGame(name, lobby);
        games.put(lobby.getName(), game);
        return game;
    }

    private Game<?, ?> doCreateGame(Constants.GAME_NAME gameName, Lobby lobby) {
        if (gameName == Constants.GAME_NAME.SECRET_HITLER) {
            return new SecretHitlerGame(lobby);
        }

        throw new UnsupportedOperationException("Unsupported game: " + gameName);
    }

    public Game<?, ?> get(String lobby) {
        return games.get(lobby);
    }

    public boolean gameExists(String game) {
        for (Constants.GAME_NAME name : Constants.GAME_NAME.values()) {
            if (name.name().equals(game))
                return true;
        }
        return false;
    }
}
