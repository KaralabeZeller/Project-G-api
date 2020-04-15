package com.nter.projectg.games.secrethitler;

import com.nter.projectg.common.Lobby;
import com.nter.projectg.games.common.Game;
import com.nter.projectg.games.secrethitler.Constants.Faction;
import com.nter.projectg.games.secrethitler.Constants.State;
import com.nter.projectg.model.secrethitler.SecretHitlerMessage;
import com.nter.projectg.model.secrethitler.SecretHitlerMessage.GameMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SecretHitlerGame extends Game<SecretHitlerMessage, SecretHitlerPlayer> {

    private static final Logger logger = LoggerFactory.getLogger(SecretHitlerGame.class);

    private List<SecretHitlerPlayer> players;
    private Assets assets;

    private State state;
    private Votes votes;

    // TODO refactor to use methods instead of fields
    private int hitlerID;
    private int chancellorID;
    private int presidentID;
    private int lastNormalPresident;
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
        state = State.ELECTION;

        if (presidentID == -1) {
            Random rand = new Random();
            presidentID = rand.nextInt(players.size());
        } else {
            int nextPresident = -1;
            int candidate = lastNormalPresident + 1;
            while (nextPresident < 0) {
                if (candidate >= players.size())
                    candidate = 0;
                if (assets.playerMap.get(candidate) == 1)
                    nextPresident = candidate;
                else
                    candidate++;
            }
            presidentID = candidate;
            lastNormalPresident = presidentID;
        }

        sendToAll(GameMessageType.PRESIDENT, getName(), players.get(presidentID).getName());

        logger.info("Elected president: " + players.get(presidentID).getName());

        nominate();
    }

    private void nominate() {
        logger.info("President [{}] nominating a chancellor", players.get(presidentID).getName());
        state = State.NOMINATION;

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
        players.get(presidentID).sendCommand(GameMessageType.QUERY_CHANCELLOR, message);
    }

    private void initializeAssets() {
        logger.debug("Initializing players and assets: {} {}", players, assets);

        players = new ArrayList<>(getPlayers());
        votes = new Votes(players.size());
        Collections.shuffle(players);

        chancellorID = -1;
        presidentID = -1;

        assets = new Assets(players);
        assets.updateNotElect(presidentID, chancellorID);

        hitlerID = -1;
        alivePlayers = players.size();

        logger.info("Initialized players and assets: {} {}", players, assets);
    }

    private void initializeFactions() {
        logger.debug("Initializing factions: {}", players);

        // TODO refactor to avoid array indexing
        for (int i = 0; i < players.size(); i++) {
            SecretHitlerPlayer player = players.get(i);

            Faction faction = assets.getFactions().get(i);
            player.setFaction(faction);

            if (faction == Faction.HITLER) {
                hitlerID = i;
            }
        }

        logger.info("Initialized factions: {}", players);
    }

    @Override
    protected SecretHitlerPlayer createPlayer(String name) {
        return new SecretHitlerPlayer(name, sendToPlayer(name));
    }

    @Override
    protected void handleGame(SecretHitlerMessage message) {
        GameMessageType type = message.getGameType();
        if (type == GameMessageType.FACTION) {
            logger.warn("Unexpected faction message: {}", message);
        } else if (type == GameMessageType.QUERY_CHANCELLOR) {
            setChancellor(message.getContent());
        } else if (type == GameMessageType.VOTE) {
            processVote(message.getSender(), message.getContent());
        } else {
            // TODO other messages
        }
    }

    private void sendToAll(GameMessageType type, String user, String content) {
        SecretHitlerMessage message = new SecretHitlerMessage();
        message.setSender(user);
        message.setGameType(type);
        message.setContent(content);
        sendToAll(message);
    }

    private void setChancellor(String player) {
        if (state != State.NOMINATION) {
            logger.warn("Message received in a false state: {}", state.name());
            return;
        }

        logger.info("Nominated chancellor: {}", player);
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getName().equals(player)) {
                chancellorID = i;
                break;
            }
        }

        sendToAll(GameMessageType.CHANCELLOR, getName(), players.get(chancellorID).getName());

        voteGovernment();
    }

    private void voteGovernment() {
        logger.info("Voting for government: {}", votes);
        state = State.VOTE;

        sendToAll(GameMessageType.VOTE, getName(), "Ja!,Nein!");
    }

    private void processVote(String player, String vote) {
        logger.debug("Processing vote: {} {} {}", player, vote, votes);

        votes.process(player, vote);
        if (votes.isFinished()) {
            logger.info("Finished voting: {}", votes);
            for (Map.Entry<String, String> entry : votes.getVotes().entrySet()) {
                // Send every vote to each player to display on the UI.
                sendToAll(GameMessageType.VOTED, entry.getKey(), entry.getValue());
            }
        }

        logger.info("Processed vote: {} {} {}", player, vote, votes);
    }

    @Override
    public void start(String user) {
        super.start(user);

        // TODO refactor to avoid array indexing
        SecretHitlerMessage hitlerMessage = new SecretHitlerMessage();
        hitlerMessage.setSender(getName());
        hitlerMessage.setGameType(GameMessageType.HITLER);
        hitlerMessage.setContent(players.get(hitlerID).getFaction().name());

        for (SecretHitlerPlayer player : players) {
            // Send faction message to session
            SecretHitlerMessage factionMessage = new SecretHitlerMessage();
            factionMessage.setSender(getName());
            factionMessage.setGameType(GameMessageType.FACTION);
            factionMessage.setContent(player.getFaction().name());
            sendToPlayer(player.getName(), factionMessage);

            // Send hitler message to fascists
            if (player.getFaction() == Faction.FASCIST) {
                sendToPlayer(player.getName(), hitlerMessage);
            }
        }

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
        factionMessage.setGameType(GameMessageType.FACTION);
        factionMessage.setContent(player.getFaction().name());
        sendToPlayer(player.getName(), factionMessage);

        // Send hitler message to fascists
        if (player.getFaction() == Faction.FASCIST) {
            SecretHitlerMessage hitlerMessage = new SecretHitlerMessage();
            hitlerMessage.setSender(getName());
            hitlerMessage.setGameType(GameMessageType.HITLER);
            hitlerMessage.setContent(players.get(hitlerID).getFaction().name());
            sendToPlayer(player.getName(), hitlerMessage);
        }

        // Send president message to session
        SecretHitlerMessage presidentMessage = new SecretHitlerMessage();
        presidentMessage.setSender(getName());
        presidentMessage.setGameType(GameMessageType.PRESIDENT);
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
