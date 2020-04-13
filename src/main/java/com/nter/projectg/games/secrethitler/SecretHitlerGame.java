package com.nter.projectg.games.secrethitler;

import com.nter.projectg.common.Lobby;
import com.nter.projectg.games.common.Game;
import com.nter.projectg.model.common.Message;
import com.nter.projectg.model.secrethitler.SecretHitlerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SecretHitlerGame extends Game<SecretHitlerMessage, SecretHitlerPlayer> {

    private static final Logger logger = LoggerFactory.getLogger(SecretHitlerGame.class);

    private List<SecretHitlerPlayer> players;
    private Assets assets;

    private Constants.SHState gameState;

    // TODO refactor to use methods instead of fields
    private int hitlerID;
    private int chancellorID;
    private int presidentID;
    private int lastNormalPresident;
    private int playerCount;
    private int alivePlayers;

    public SecretHitlerGame(Lobby lobby) {
        super(lobby, "Secret Hitler", 5, 10);

        logger.debug("Initializing Secret Hitler: {}", this);

        initializeAssets();
        initializeFactions();
        electPresident();
        logger.info("Initialized Secret Hitler: {}", this);
    }

    private void electPresident() {
        logger.info("Electing president");

        if (presidentID == -1) {
            Random rand = new Random();
            presidentID = rand.nextInt(players.size());
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

        // Send message to session
        SecretHitlerMessage message = new SecretHitlerMessage();
        message.setSender(getName());
        message.setGameMessageType(SecretHitlerMessage.GameMessageType.PRESIDENT);
        message.setContent(players.get(presidentID).getName());
        sendToAll(message);

        gameState = Constants.SHState.NOMINATION;
        nominate();
        
        logger.info("Elected president: " + players.get(presidentID).getName());

    }

    private void nominate() {

        logger.info("President [{}] nominating a chancellor", players.get(presidentID).getName());
        List<String> playerList = new ArrayList<>();

        for (int i = 0; i < players.size(); i++) {
            if(alivePlayers <= 5) {
                if (i != assets.nonElectables[1] && i != presidentID) {
                    if (assets.playerMap.get(i) == 1) {
                        SecretHitlerPlayer u = players.get(i);
                        playerList.add(u.getName());
                    }
                }
            }else {
                if (i != assets.nonElectables[0] && i != assets.nonElectables[1] && i != presidentID) {
                    if (assets.playerMap.get(i) == 1) {
                        SecretHitlerPlayer u = players.get(i);
                        playerList.add(u.getName());
                    }
                }
            }
        }

        String message = String.join(",", playerList);
        logger.info("Nominable players for chancellor {}", message);
        players.get(presidentID).sendCommand(SecretHitlerMessage.GameMessageType.QUERY_CHANCELLOR, message);

    }

    private void initializeAssets() {
        logger.debug("Initializing players and assets: {} {}", players, assets);

        players = new ArrayList<>(getPlayers());
        Collections.shuffle(players);

        assets = new Assets(players);
        assets.updateNotElect(presidentID, chancellorID);

        hitlerID     = -1;
        chancellorID = -1;
        presidentID  = -1;
        playerCount  = players.size();
        alivePlayers = playerCount;

        logger.info("Initialized players and assets: {} {}", players, assets);
    }

    private void initializeFactions() {
        logger.debug("Initializing factions: {}", players);

        // TODO refactor to avoid array indexing
        for (int i = 0; i < players.size(); i++) {
            SecretHitlerPlayer player = players.get(i);

            Constants.Faction faction = assets.getFactions().get(i);
            player.setFaction(faction);

            if (faction == Constants.Faction.HITLER) {
                hitlerID = i;
                // TODO implement
                // sendHitlerToFascists();
            }
        }

        logger.info("Initialized factions: {}", players);
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
                ", playersShuffled=" + players +
                ", hitlerID=" + hitlerID +
                ", chancellorID=" + chancellorID +
                ", presidentID=" + presidentID +
                '}';
    }

}
