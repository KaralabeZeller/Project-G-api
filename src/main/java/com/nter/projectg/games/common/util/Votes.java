package com.nter.projectg.games.common.util;

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

    public int getFrequency(String vote) {
        return Collections.frequency(votes.values(), vote);
    }

    public boolean isFinished(int players) {
        return votes.size() == players;
    }

    public void clear() {
        votes.clear();
    }

    public void process(String player, String vote) {
        votes.put(player, vote);
    }

    @Override
    public String toString() {
        return "Votes{" +
                "votes=" + votes +
                '}';
    }

}
