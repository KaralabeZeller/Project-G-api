package com.nter.projectg.games.secrethitler;

import com.nter.projectg.common.Lobby;
import com.nter.projectg.games.common.Game;
import com.nter.projectg.model.secrethitler.SecretHitlerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SecretHitlerGame extends Game<SecretHitlerMessage, SecretHitlerPlayer> {

    private static final Logger logger = LoggerFactory.getLogger(SecretHitlerGame.class);

    private List<SecretHitlerPlayer> playersShuffled;
    private Assets assets;

    private Constants.SHState gameState;

    // TODO refactor to use methods instead of fields
    private int hitlerID;
    private int chancellorID;
    private int presidentID;
    private int lastNormalPresident;
    private int playerCount;

    public SecretHitlerGame(Lobby lobby) {
        super(lobby, "Secret Hitler", 5, 10);

        logger.debug("Initializing Secret Hitler: {}", this);

        initializeAssets();
        playerCount = playersShuffled.size();
        initializeFactions();
        electPresident();
        logger.info("Initialized Secret Hitler: {}", this);
    }

    private void electPresident() {

        if (presidentID == -1) {
            Random rand = new Random();
            presidentID = rand.nextInt(playersShuffled.size());
        } else {

            if (presidentID == -1) {
                Random rand = new Random();
                presidentID = rand.nextInt(playerCount);
            } else {

                int nextPresident = -1;
                int candidate = lastNormalPresident + 1;

                while (nextPresident < 0) {
                    if (candidate >= playerCount)
                        candidate = 0;

                    if (assets.playerMap.get(candidate) == 1)
                        nextPresident = candidate;
                    else
                        candidate++;
                }

                presidentID = candidate;

            }
            lastNormalPresident = presidentID;
        }
        gameState = Constants.SHState.NOMINATION;

        // Send message to session
        SecretHitlerMessage message = new SecretHitlerMessage();
        message.setSender(getName());
        message.setGameMessageType(SecretHitlerMessage.GameMessageType.PRESIDENT);
        message.setContent(playersShuffled.get(presidentID).getName());
        sendToAll(message);

        logger.info("President: " + playersShuffled.get(presidentID).getName());

    }

    private void initializeAssets() {
        logger.debug("Initializing players and assets: {} {}", playersShuffled, assets);

        playersShuffled = new ArrayList<>(getPlayers());
        Collections.shuffle(playersShuffled);

        assets = new Assets(playersShuffled);
        assets.updateNotElect(presidentID, chancellorID);

        hitlerID = -1;
        chancellorID = -1;
        presidentID = -1;

        logger.info("Initialized players and assets: {} {}", playersShuffled, assets);
    }

    private void initializeFactions() {
        logger.debug("Initializing factions: {}", playersShuffled);

        // TODO refactor to avoid array indexing
        for (int i = 0; i < playersShuffled.size(); i++) {
            SecretHitlerPlayer player = playersShuffled.get(i);

            Constants.Faction faction = assets.getFactions().get(i);
            player.setFaction(faction);

            if (faction == Constants.Faction.HITLER) {
                hitlerID = i;
                // TODO implement
                // sendHitlerToFascists();
            }
        }

        logger.info("Initialized factions: {}", playersShuffled);
    }

    @Override
    protected SecretHitlerPlayer createPlayer(String name) {
        return new SecretHitlerPlayer(name, sendToPlayer(name));
    }

    @Override
    public void run() {
        // TODO
    }

    @Override
    public String toString() {
        return "SecretHitler{" +
                "super=" + super.toString() +
                ", playersShuffled=" + playersShuffled +
                ", hitlerID=" + hitlerID +
                ", chancellorID=" + chancellorID +
                ", presidentID=" + presidentID +
                '}';
    }

}
