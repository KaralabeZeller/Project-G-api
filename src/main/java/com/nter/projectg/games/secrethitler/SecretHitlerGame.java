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

    private List<SecretHitlerPlayer> players;
    private Assets assets;
    private Votes votes;

    private Constants.SHState gameState;

    // TODO refactor to use methods instead of fields
    private int hitlerID;
    private int chancellorID;
    private int presidentID;
    private int lastNormalPresident;
    private int playerCount;
    private int alivePlayers;

    public SecretHitlerGame(Lobby lobby) {
        super(lobby, "SecretHitler", 5, 10);

        logger.debug("Initializing Secret Hitler: {}", this);

        initializeAssets();
        initializeFactions();
        logger.info("Initialized Secret Hitler: {}", this);
    }

    private void electPresident() {
        logger.info("Electing president");

        if (presidentID == -1) {
            Random rand = new Random();
            presidentID = rand.nextInt(players.size());
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

            lastNormalPresident = presidentID;
        }

        sendToAll(SecretHitlerMessage.GameMessageType.PRESIDENT, getName(), players.get(presidentID).getName());

        gameState = Constants.SHState.NOMINATION;
        nominate();

        logger.info("Elected president: " + players.get(presidentID).getName());
    }

    private void nominate() {

        logger.info("President [{}] nominating a chancellor", players.get(presidentID).getName());
        List<String> playerList = new ArrayList<>();

        for (int i = 0; i < players.size(); i++) {
            if (alivePlayers <= 5) {
                if (i != assets.nonElectables[1] && i != presidentID) {
                    if (assets.playerMap.get(i) == 1) {
                        SecretHitlerPlayer u = players.get(i);
                        playerList.add(u.getName());
                    }
                }
            } else {
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

        //TODO implement - state change in separate function with logging
        gameState = Constants.SHState.NOMINATION;

    }

    private void initializeAssets() {
        logger.debug("Initializing players and assets: {} {}", players, assets);

        players = new ArrayList<>(getPlayers());
        votes = new Votes(this);
        Collections.shuffle(players);

        chancellorID = -1;
        presidentID = -1;

        assets = new Assets(players);
        assets.updateNotElect(presidentID, chancellorID);

        hitlerID = -1;
        playerCount = players.size();
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
    protected void processMessage(SecretHitlerMessage message) {
        if (message.getGameMessageType() == SecretHitlerMessage.GameMessageType.FACTION) {
            // nothing to do
        } else if (message.getGameMessageType() == SecretHitlerMessage.GameMessageType.QUERY_CHANCELLOR) {
            setChancellor(message.getContent());
        } else if (message.getGameMessageType() == SecretHitlerMessage.GameMessageType.VOTE) {
            votes.process(message.getSender(), message.getContent());
            if (votes.voteFinished()) {
                processVotes();
            }
        } else {
            // TODO other messages
        }
    }

    private void processVotes() {
        logger.info("Voting has finished");
        votes.getVotes().forEach((key, value) -> sendToAll(SecretHitlerMessage.GameMessageType.VOTED, key, value));
    }

    private void sendToAll(SecretHitlerMessage.GameMessageType type, String user, String content) {
        SecretHitlerMessage message = new SecretHitlerMessage();
        message.setSender(user);
        message.setGameMessageType(type);
        message.setContent(content);
        sendToAll(message);
    }

    private void setChancellor(String player) {
        if (gameState != Constants.SHState.NOMINATION) {
            logger.warn("Message received in a false state: {}", gameState.name());
            return;
        }

        logger.info("Nominated chancellor: {}", player);
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getName().equals(player)) {
                chancellorID = i;
                break;
            }
        }

        sendToAll(SecretHitlerMessage.GameMessageType.CHANCELLOR, getName(), players.get(chancellorID).getName());

        gameState = Constants.SHState.VOTE;
        voteGovernment();

    }

    private void voteGovernment() {
        logger.info("Voting for government");
        for (SecretHitlerPlayer player : players) {
            player.sendCommand(SecretHitlerMessage.GameMessageType.VOTE, "Ja!,Nein!");
        }
    }

    @Override
    public void start(String user) {
        super.start(user);

        SecretHitlerPlayer player = findPlayer(user);

        // Send faction message to session
        SecretHitlerMessage factionMessage = new SecretHitlerMessage();
        factionMessage.setSender(getName());
        factionMessage.setGameMessageType(SecretHitlerMessage.GameMessageType.FACTION);
        factionMessage.setContent(player.getFaction().name());
        sendToPlayer(player.getName(), factionMessage);

        electPresident();
    }

    // TODO implement restore state / messages on reconnect
    @Override
    public void reconnect(String user) {
        super.reconnect(user);

        SecretHitlerPlayer player = findPlayer(user);

        // Send faction message to session
        SecretHitlerMessage factionMessage = new SecretHitlerMessage();
        factionMessage.setSender(getName());
        factionMessage.setGameMessageType(SecretHitlerMessage.GameMessageType.FACTION);
        factionMessage.setContent(player.getFaction().name());
        sendToPlayer(player.getName(), factionMessage);

        // Send president message to session
        SecretHitlerMessage presidentMessage = new SecretHitlerMessage();
        presidentMessage.setSender(getName());
        presidentMessage.setGameMessageType(SecretHitlerMessage.GameMessageType.PRESIDENT);
        presidentMessage.setContent(players.get(presidentID).getName());
        sendToPlayer(player.getName(), presidentMessage);
    }

    @Override
    public void stop() {
        super.stop();
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
