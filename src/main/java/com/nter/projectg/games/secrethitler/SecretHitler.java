package com.nter.projectg.games.secrethitler;

import com.nter.projectg.common.Lobby;
import com.nter.projectg.games.common.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SecretHitler extends Game<SHMessage, UserSecretHitler> {

    private static final Logger logger = LoggerFactory.getLogger(SecretHitler.class);

    private List<UserSecretHitler> players;
    private Assets assets;

    private int hitlerID;
    private int chancellorID;
    private int presidentID;

    public SecretHitler(Lobby lobby) {
        super(lobby, "Secret Hitler", 5, 10);

        logger.debug("Initializing Secret Hitler: {}", this);
        init();
        sendFactions();
        logger.info("Initialized Secret Hitler: {}", this);
    }

    private void init() {
        logger.debug("Initializing players and assets: {} {}", assets, players);

        if (getPlayerCount() < getMinPlayers() || getPlayerCount() >= getMaxPlayers())
            logger.warn("Player count is not in the interval!");

        players = new ArrayList<>(getPlayers());
        Collections.shuffle(players);

        assets = new Assets(players);
        assets.updateNotElect(presidentID, chancellorID);

        hitlerID = -1;
        chancellorID = -1;
        presidentID = -1;

        logger.info("Initialized players and assets: {} {}", assets, players);
    }

    private void sendFactions() {
        logger.debug("Initializing factions: hitlerId={} {}", hitlerID, players);

        for (int i = 0; i < players.size(); i++) {
            UserSecretHitler player = players.get(i);

            // TODO investigate - null factions
            Constants.Faction f = assets.getFactions().get(i);
            player.setFaction(f);

            if (f == Constants.Faction.HITLER)
                hitlerID = i;
        }
        logger.info("Initialized factions: hitlerId={} {}", hitlerID, players);

        //sendHitlerToFascists();
    }

    @Override
    protected UserSecretHitler createClient(String name) {
        return new UserSecretHitler(name, sendToPlayer(name));
    }

    @Override
    public void run() {
        // TODO
    }

    @Override
    public String toString() {
        return "SecretHitler{" +
                "super=" + super.toString() +
                ", players=" + players +
                ", hitlerID=" + hitlerID +
                ", chancellorID=" + chancellorID +
                ", presidentID=" + presidentID +
                '}';
    }
}
