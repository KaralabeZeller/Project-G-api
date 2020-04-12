package com.nter.projectg.games.secrethitler;

import com.nter.projectg.common.Lobby;
import com.nter.projectg.games.common.Game;
import com.nter.projectg.model.secrethitler.SecretHitlerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SecretHitlerGame extends Game<SecretHitlerMessage, SecretHitlerPlayer> {

    private static final Logger logger = LoggerFactory.getLogger(SecretHitlerGame.class);

    private List<SecretHitlerPlayer> playersShuffled;
    private Assets assets;

    // TODO refactor to use methods instead of fields
    private int hitlerID;
    private int chancellorID;
    private int presidentID;

    public SecretHitlerGame(Lobby lobby) {
        super(lobby, "Secret Hitler", 5, 10);

        logger.debug("Initializing Secret Hitler: {}", this);
        initializeAssets();
        initializeFactions();
        logger.info("Initialized Secret Hitler: {}", this);
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
