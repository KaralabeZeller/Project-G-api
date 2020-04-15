package com.nter.projectg.games.secrethitler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Votes {

    private final int players;
    private final Map<String, String> votes;

    public Votes(int players) {
        this.players = players;
        votes = new HashMap<>(players);
    }

    public Map<String, String> getVotes() {
        return Collections.unmodifiableMap(votes);
    }

    public boolean isFinished() {
        return votes.size() == players;
    }

    public void process(String player, String vote) {
        votes.put(player, vote);
    }

    @Override
    public String toString() {
        return "Votes{" +
                "players=" + players +
                ", votes=" + votes +
                '}';
    }

}
