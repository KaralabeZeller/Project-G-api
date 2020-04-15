package com.nter.projectg.games.secrethitler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Votes {
    private static final Logger logger = LoggerFactory.getLogger(Votes.class);

    SecretHitlerGame game;
    Map<String, String> userVote;
    Map<String, String> voteUser;

    public Votes(SecretHitlerGame secretHitlerGame) {
        this.game = secretHitlerGame;
        userVote = new HashMap<>();
        voteUser = new HashMap<>();
    }

    public void process(String sender, String content) {
        logger.info("Recieved vote from {}: {}", sender, content);
        userVote.put(sender, content);
        voteUser.put(content, sender);
    }

    public boolean voteFinished() {
        return userVote.keySet().size() == game.getPlayerCount() ? true : false;
    }

    public Map<String, String> getVotes() {
        return userVote;
    }
}
