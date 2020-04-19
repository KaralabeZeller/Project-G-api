package com.nter.projectg.games.secrethitler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Votes {

    private final Map<String, String> votes;

    public Votes() {
        votes = new HashMap<>();
    }

    public Map<String, String> getVotes() {
        return Collections.unmodifiableMap(votes);
    }

    public boolean isFinished(int players) {
        return votes.size() == players;
    }

    public void process(String player, String vote) {
        votes.put(player, vote);
    }

    public void clear() {
        votes.clear();
    }

    @Override
    public String toString() {
        return "Votes{" +
                ", votes=" + votes +
                '}';
    }
}
