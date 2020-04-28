package com.nter.projectg.games.common;

import com.nter.projectg.games.common.util.Constants;
import com.nter.projectg.games.secrethitler.SecretHitlerGame;
import com.nter.projectg.lobby.Lobby;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameHandler {

    private final Map<String, Game<?, ?>> games = new ConcurrentHashMap<>();
    private Random rand = new Random();

    public Game<?, ?> createGame(Constants.GAME_NAME gameName, Lobby lobby) {
        Game<?, ?> game = null;

        switch (gameName) {
            case SECRET_HITLER:
                game = new SecretHitlerGame(lobby);
                break;
        }

        games.put(lobby.getName(), game);
        return game; //TODO refactor the null return value

    }

    public Game get(String lobby) {
        return games.get(lobby);
    }
}
