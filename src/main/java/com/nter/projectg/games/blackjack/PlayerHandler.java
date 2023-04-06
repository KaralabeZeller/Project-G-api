package com.nter.projectg.games.blackjack;

import com.nter.projectg.games.secrethitler.SecretHitlerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlayerHandler {
    private final Map<String, BlackJackPlayer> playerMap;

    public PlayerHandler(List<BlackJackPlayer> players) {
        playerMap = players.stream().collect(Collectors.toMap(p -> p.getName(), p -> p));
    }

    public int getPlayerCount() {
        return playerMap.size();
    }


    public List<BlackJackPlayer> getPlayers() {
        return new ArrayList<>(playerMap.values());
    }

    public BlackJackPlayer getNextPlayer(SecretHitlerPlayer player) {
        List<BlackJackPlayer> players = getPlayers();
        players.add(players.get(0));
        return players.get(players.indexOf(player) + 1);
    }

    public BlackJackPlayer getPlayerByName(String name) {
        return playerMap.get(name);
    }


    @Override
    public String toString() {
        return "PlayerHandler{" +
                "playerMap=" + playerMap +
                '}';
    }
}
