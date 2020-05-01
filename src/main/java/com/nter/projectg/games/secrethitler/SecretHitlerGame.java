package com.nter.projectg.games.secrethitler;

import com.nter.projectg.games.common.Game;
import com.nter.projectg.games.common.Player;
import com.nter.projectg.games.common.util.Votes;
import com.nter.projectg.games.secrethitler.Constants.Faction;
import com.nter.projectg.games.secrethitler.Constants.Policy;
import com.nter.projectg.games.secrethitler.Constants.State;
import com.nter.projectg.lobby.Lobby;
import com.nter.projectg.model.secrethitler.SecretHitlerMessage;
import com.nter.projectg.model.secrethitler.SecretHitlerMessage.GameMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO refactor to avoid array indexing
public class SecretHitlerGame extends Game<SecretHitlerMessage, SecretHitlerPlayer> {

    private static final Logger logger = LoggerFactory.getLogger(SecretHitlerGame.class);

    private PlayerHandler playerHandler;

    private Assets assets;

    private State state;
    private Votes votes;

    private boolean specialElection;

    public SecretHitlerGame(Lobby lobby) {
        super(lobby, "SecretHitler", 5, 10);

        logger.debug("Initializing Secret Hitler: {}", this);
        initializeAssets();
        initializeFactions();
        logger.info("Initialized Secret Hitler: {}", this);
    }

    @Override
    protected SecretHitlerPlayer createPlayer(String name) {
        return new SecretHitlerPlayer(name, sendToPlayer(name));
    }

    private void initializeAssets() {
        playerHandler = new PlayerHandler(getPlayers());
        logger.debug("Initializing players and assets: {} {}", playerHandler, assets);

        votes = new Votes();

        specialElection = false;

        List<SecretHitlerPlayer> shuffledPlayers = new ArrayList<>(playerHandler.getPlayers());
        Collections.shuffle(shuffledPlayers);
        assets = new Assets(shuffledPlayers);

        logger.info("Initialized players and assets: {} {}", playerHandler, assets);
    }

    private void initializeFactions() {
        logger.debug("Initializing factions: {}", playerHandler);

        for (SecretHitlerPlayer player : playerHandler.getPlayers()) {
            Faction faction = assets.getNextFaction();
            player.setFaction(faction);
        }

        logger.info("Initialized factions: {}", playerHandler);
    }

    private void electPresident() {
        logger.info("Electing president");
        state = State.ELECTION;

        if (specialElection) {
            playerHandler.setPreviousPresident(playerHandler.getPresident());
            playerHandler.getPresident().setPresident(false);
            playerHandler.getSpecialPresident().setPresident(true);
            specialElection = false;
        } else {
            SecretHitlerPlayer candidate;
            if (playerHandler.existsPresident()) {
                playerHandler.setPreviousPresident(playerHandler.getPresident());
                playerHandler.getPresident().setPresident(false);
            }

            candidate = playerHandler.getNextPlayer(playerHandler.getLastNormalPresident());

            while (!candidate.isAlive()) {
                candidate = playerHandler.getNextPlayer(candidate);
            }

            candidate.setPresident(true);

            playerHandler.setLastNormalPresident(candidate);
        }

        logger.info("Elected president: {}", playerHandler.getPresident().getName());
        SecretHitlerMessage presidentMessage = buildGameMessage(GameMessageType.PRESIDENT, playerHandler.getPresident().getName());
        sendToAll(presidentMessage);

        nominate();
    }

    private void nominate() {
        SecretHitlerPlayer president = playerHandler.getPresident();
        logger.info("President nominating a chancellor: {}", president);
        state = State.NOMINATION;

        List<SecretHitlerPlayer> eligiblePlayers = new ArrayList<>();
        for (SecretHitlerPlayer player : playerHandler.getPlayers()) {
            if (playerHandler.getAlivePlayerCount() <= 5) {
                if (!player.equals(playerHandler.getPreviousPresident()) && !player.isPresident() && player.isAlive()) {
                    eligiblePlayers.add(player);
                }
            } else {
                if (!player.equals(playerHandler.getPreviousPresident()) && !player.equals(playerHandler.getPreviousChancellor()) && !player.isPresident() && player.isAlive()) {
                    eligiblePlayers.add(player);
                }
            }
        }

        logger.info("Eligible players for chancellor: {}", eligiblePlayers);
        SecretHitlerMessage chancellorMessage = buildGameMessage(GameMessageType.QUERY_CHANCELLOR, playersAsString(eligiblePlayers));
        sendToPlayer(president.getName(), chancellorMessage);
        sendStatus("President " + playerHandler.getPresident().getName() + " is nominating a chancellor");
    }

    @Override
    protected void handleGame(SecretHitlerMessage message) {
        if (state == State.FINISHED) {
            logger.warn("Game is already finished!");
            return;
        }

        GameMessageType type = message.getGameType();
        if (type == GameMessageType.FACTION) {
            logger.warn("Unexpected faction message: {}", message);
        } else if (type == GameMessageType.QUERY_CHANCELLOR) {
            setChancellor(message.getContent());
        } else if (type == GameMessageType.VOTE) {
            processVote(message.getSender(), message.getContent());
        } else if (type == GameMessageType.POLICIES) {
            processPolicies(message.getSender(), message.getContent());
        } else if (type == GameMessageType.POLICY) {
            enactPolicies(message.getContent());
        } else if (type == GameMessageType.KILL) {
            processKill(message.getContent());
        } else if (type == GameMessageType.INVESTIGATE) {
            processInvestigate(message.getContent());
        } else if (type == GameMessageType.SPECIAL_ELECTION) {
            processSpecialElection(message.getContent());
        } else {
            logger.warn("Unexpected message: {}", message);
        }
    }

    private void sendStatus(String status) {
        SecretHitlerMessage statusMessage = buildGameMessage(GameMessageType.STATE, status);
        sendToAll(statusMessage);
    }

    private void setChancellor(String player) {
        // TODO implement - state watchers into all process functions
        if (state != State.NOMINATION) {
            logger.warn("Message received in a false state: {}", state.name());
            return;
        }

        logger.info("Nominated chancellor: {}", player);
        if (playerHandler.existsChancellor()) {
            playerHandler.setPreviousChancellor(playerHandler.getChancellor());
            playerHandler.getChancellor().setChancellor(false);
        }
        playerHandler.getPlayerByName(player).setChancellor(true);

        SecretHitlerMessage chancellorMessage = buildGameMessage(GameMessageType.CHANCELLOR, playerHandler.getChancellor().getName());
        sendToAll(chancellorMessage);
        sendStatus("President " + playerHandler.getPresident().getName() + " has nominated " + playerHandler.getChancellor().getName() + " as a chancellor");

        scheduleVoteGovernment();
    }

    private void voteGovernment() {
        logger.info("Voting for government: {}", votes);
        state = State.VOTE;

        SecretHitlerMessage voteMessage = buildGameMessage(GameMessageType.VOTE, "Ja!,Nein!");
        for (SecretHitlerPlayer player : playerHandler.getPlayers()) {
            if (player.isAlive())
                sendToPlayer(player.getName(), voteMessage);
        }

        sendStatus("Vote for the government: President - " + playerHandler.getPresident().getName() + ", Chancellor - " + playerHandler.getChancellor().getName());
    }

    private void processVote(String player, String vote) {
        logger.debug("Processing vote: {} {} {}", player, vote, votes);

        votes.process(player, vote);

        if (votes.isFinished(playerHandler.getAlivePlayerCount())) {
            logger.info("Finished voting: {}", votes);
            for (Map.Entry<String, String> entry : votes.getVotes().entrySet()) {
                // Send every vote to each player to display on the UI.
                SecretHitlerMessage votedMessage = buildGameMessage(GameMessageType.VOTED, entry.getKey(), entry.getValue());
                sendToAll(votedMessage);
            }

            // TODO move to Votes
            int jaVotes = votes.getFrequency("Ja!");
            if (jaVotes - 1 >= playerHandler.getAlivePlayerCount() / 2) {
                logger.info("Vote result: Ja!");
                sendStatus("Vote result: Ja!");
                assets.electionTracker = 0;
                moveTracker();

                if (assets.getPolicyCount(Policy.FASCIST) >= 3) {
                    // TODO move to checkAssets
                    if (playerHandler.getChancellor().equals(playerHandler.getHitler())) {
                        logger.info("FASCIST win - Hitler is the chancellor");
                        sendStatus("FASCIST victory - Hitler is the chancellor");
                        sendVictory(Faction.FASCIST);
                        state = State.FINISHED;
                        return;
                    }
                }

                state = State.ENACTMENT;
                scheduleSelectPolicy();
            } else {
                logger.info("Votes result: Nein!");
                sendStatus("Vote result: Nein!");
                assets.electionTracker++;
                moveTracker();
                state = State.ELECTION;
                scheduleCheckAssets();
            }

            votes.clear();
        }

        logger.debug("Processed vote: {} {} {}", player, vote, votes);
    }

    private void moveTracker() {
        SecretHitlerMessage trackerMessage = buildGameMessage(GameMessageType.TRACKER, Integer.toString(assets.electionTracker));
        sendToAll(trackerMessage);
    }

    private void processSpecialElection(String content) {
        playerHandler.setSpecialPresident(playerHandler.getPlayerByName(content));
        specialElection = true;
    }

    private void selectPolicy() {
        List<Policy> policies = assets.getTopPolicies();
        logger.info("Policies for the president: {}", policies);
        SecretHitlerMessage policyMessage = buildGameMessage(GameMessageType.POLICIES, policiesAsString(policies));
        sendToPlayer(playerHandler.getPresident().getName(), policyMessage);
        sendStatus("President " + playerHandler.getPresident().getName() + " is selecting policies to pass to the chancellor");

        // TODO add to processPolicies
        /*
        if (nominee.equals("VETO"))
        {
            String vetoed = players.get(presidentID).client.processQuery("VETO?");
            if (vetoed.equals("Ja!"))
                veto = true;
        }
        if (!veto) {
            if (nominee.equals("FASCIST")) {
                assets.enactPolicy(Policy.FASCIST);
                ui.enactPolicy(Policy.FASCIST);
            } else {
                assets.enactPolicy(Policy.LIBERAL);
                ui.enactPolicy(Policy.LIBERAL);
            }
            logger.info("--Policy enacted by government: {}", nominee);
            assets.electionTracker = 0;
            //moveTracker();
        } else {
            logger.info("--Policy vetoed by government!");
            assets.electionTracker++;
            //moveTracker();
        }

        state = State.ELECTION;
        //clearChancellor(chancellorID);
         */
    }

    private void processPolicies(String player, String policies) {
        if (state != State.ENACTMENT) {
            logger.warn("Message received in a false state: {}", state.name());
            return;
        }

        logger.info("Passing policies from president to chancellor: {} {} {}", policies, playerHandler.getPresident(), playerHandler.getChancellor());
        SecretHitlerMessage policyMessage = buildGameMessage(GameMessageType.POLICY, policies);
        sendToPlayer(playerHandler.getChancellor().getName(), policyMessage);
        sendStatus("Chancellor " + playerHandler.getChancellor().getName() + " is selecting a policy to be enacted");
    }

    private void enactPolicies(String policy) {
        if (state != State.ENACTMENT) {
            logger.warn("Message received in a false state: {}", state.name());
            return;
        }

        logger.info("Enacted policy by chancellor: {} {} {}", policy, playerHandler.getPresident(), playerHandler.getChancellor());
        SecretHitlerMessage policyMessage = buildGameMessage(GameMessageType.ENACTED_POLICY, policy);
        sendToAll(policyMessage);
        sendStatus("Chancellor " + playerHandler.getChancellor().getName() + " has enacted a " + policy + " policy");

        assets.enactPolicy(Policy.valueOf(policy));
        state = State.ELECTION;
        scheduleCheckAssets();
    }

    @Override
    public void start() {
        super.start();

        SecretHitlerMessage hitlerMessage = buildGameMessage(GameMessageType.HITLER, playerHandler.getHitler().getName());
        for (SecretHitlerPlayer player : playerHandler.getPlayers()) {
            // Send faction message to session
            SecretHitlerMessage factionMessage = buildGameMessage(GameMessageType.FACTION, player.getFaction().name());
            sendToPlayer(player.getName(), factionMessage);

            // Send hitler message to fascists
            if (player.getFaction() == Faction.FASCIST) {
                sendToPlayer(player.getName(), hitlerMessage);
            }

            // Send other fascists to hitler (5-6 players)
            if (playerHandler.getPlayerCount() == 5 || playerHandler.getPlayerCount() == 6) {
                if (player.getFaction() == Faction.FASCIST) {
                    SecretHitlerMessage fascistMessage = buildGameMessage(GameMessageType.FELLOW_FASCIST, player.getName());
                    sendToPlayer(playerHandler.getHitler().getName(), fascistMessage);
                }
            }
        }

        electPresident();
    }

    @Override
    public void reconnect(String user) {
        // TODO implement restore state / messages on reconnect
        super.reconnect(user);

        SecretHitlerPlayer player = findPlayer(user);

        // Send faction message to session
        SecretHitlerMessage factionMessage = buildGameMessage(GameMessageType.FACTION, player.getFaction().name());
        sendToPlayer(player.getName(), factionMessage);

        // Send hitler message to fascists
        if (player.getFaction() == Faction.FASCIST) {
            SecretHitlerMessage hitlerMessage = buildGameMessage(GameMessageType.HITLER, playerHandler.getHitler().getFaction().name());
            sendToPlayer(player.getName(), hitlerMessage);
        }

        // Send president message to session
        SecretHitlerMessage presidentMessage = buildGameMessage(GameMessageType.PRESIDENT, playerHandler.getPresident().getName());
        sendToPlayer(player.getName(), presidentMessage);
    }

    @Override
    public void stop() {
        super.stop();
    }

    private void checkAssets() {
        if (assets.electionTracker == 3) {
            logger.info("Election tracker: 3 - Top policy will be enacted");
            sendStatus("Election tracker: 3 - Top policy will be enacted");

            Policy topPolicy = assets.enactTopPolicy();
            SecretHitlerMessage policyMessage = buildGameMessage(GameMessageType.ENACTED_POLICY, topPolicy.name());
            getTimer().delay(() -> sendToAll(policyMessage), 5);
            sendStatus("Enacted top policy: " + topPolicy.name());

            logger.info("Policy enacted: {}", topPolicy);
            assets.electionTracker = 0;
            moveTracker();
        }

        if (getPlayers().size() < 7) { // 5 - 6 PLAYERS
            if (assets.getPolicyCount(Policy.FASCIST) == 3) { // PEAK TOP POLICY
                if (assets.activePowers.isEmpty()) {
                    List<Policy> policies = assets.getTopPolicies();
                    logger.info("Sending top policies for the president to peek: {}", policies);
                    sendStatus("Sending top policies for the president to peek");
                    SecretHitlerMessage policyMessage = buildGameMessage(GameMessageType.TOP_POLICIES, policiesAsString(policies));
                    sendToPlayer(playerHandler.getPresident().getName(), policyMessage);

                    assets.usePower();
                    scheduleElectPresident();
                    return;
                }
            }
            if (assets.getPolicyCount(Policy.FASCIST) == 4 && assets.activePowers.size() == 1) { // KILL SOMEONE
                killUser();
                assets.usePower();
                return;
            }
            if (assets.getPolicyCount(Policy.FASCIST) == 5 && assets.activePowers.size() == 2) {
                killUser();
                assets.usePower();
                return;
            }
        }

        if (getPlayers().size() == 7 || getPlayers().size() == 8) { // 7 - 8 PLAYERS

            if (assets.getPolicyCount(Policy.FASCIST) == 2) { // INVESTIGATE LOYALTY
                if (assets.activePowers.isEmpty()) {
                    investigate();
                    assets.usePower();
                    return;
                }
            }
            if (assets.getPolicyCount(Policy.FASCIST) == 3) { // SPECIAL ELECTION
                if (assets.activePowers.size() == 1) {
                    specialElection();
                    assets.usePower();
                    return;
                }
            }
            if (assets.getPolicyCount(Policy.FASCIST) == 4) {
                if (assets.activePowers.size() == 2) {
                    killUser();
                    assets.usePower();
                    return;
                }
            }
            if (assets.getPolicyCount(Policy.FASCIST) == 5) {
                if (assets.activePowers.size() == 3) {
                    killUser();
                    assets.usePower();
                    return;
                }
            }
        }

        if (getPlayers().size() == 9 || getPlayers().size() == 10) { // 9 - 10 PLAYERS

            if (assets.getPolicyCount(Policy.FASCIST) == 1) { // INVESTIGATE LOYALTY
                if (assets.activePowers.isEmpty()) {
                    investigate();
                    assets.usePower();
                    return;
                }
            }
            if (assets.getPolicyCount(Policy.FASCIST) == 2) { // INVESTIGATE LOYALTY
                if (assets.activePowers.size() == 1) {
                    investigate();
                    assets.usePower();
                    return;
                }
            }
            if (assets.getPolicyCount(Policy.FASCIST) == 3) { // SPECIAL ELECTION
                if (assets.activePowers.size() == 2) {
                    specialElection();
                    assets.usePower();
                    return;
                }
            }
            if (assets.getPolicyCount(Policy.FASCIST) == 4) {
                if (assets.activePowers.size() == 3) {
                    killUser();
                    assets.usePower();
                    return;
                }
            }
            if (assets.getPolicyCount(Policy.FASCIST) == 5) {
                if (assets.activePowers.size() == 4) {
                    killUser();
                    assets.usePower();
                    return;
                }
            }
        }

        if (assets.getPolicyCount(Policy.FASCIST) == 6) {
            state = State.FINISHED;
            logger.info("FASCIST victory");
            sendStatus("FASCIST victory");
            sendVictory(Faction.FASCIST);
            return;
        }

        if (assets.getPolicyCount(Policy.LIBERAL) == 5) {
            state = State.FINISHED;
            logger.info("LIBERAL victory");
            sendStatus("LIBERAL victory");
            sendVictory(Faction.LIBERAL);
            return;
        }

        scheduleElectPresident();
    }

    private void specialElection() {
        List<SecretHitlerPlayer> eligiblePlayers = new ArrayList<>();
        for (SecretHitlerPlayer player : playerHandler.getPlayers()) {
            if (!player.isPresident() && player.isAlive()) {
                eligiblePlayers.add(player);
            }
        }

        logger.info("Special election for the president: {}", eligiblePlayers);
        SecretHitlerMessage specialElection = buildGameMessage(GameMessageType.SPECIAL_ELECTION, playersAsString(eligiblePlayers));
        sendToPlayer(playerHandler.getPresident().getName(), specialElection);
        sendStatus("President " + playerHandler.getPresident().getName() + " is electing the next president");

        scheduleElectPresident();
    }

    private void killUser() {
        logger.info("Query user to be killed by the president");

        List<SecretHitlerPlayer> killablePlayers = new ArrayList<>();
        for (SecretHitlerPlayer player : playerHandler.getPlayers()) {
            if (!player.isPresident() && player.isAlive()) {
                killablePlayers.add(player);
            }
        }

        logger.info("Kill for the president: {}", killablePlayers);
        sendStatus("President " + playerHandler.getPresident().getName() + " is choosing a player to be killed");

        SecretHitlerMessage killMessage = buildGameMessage(GameMessageType.KILL, playersAsString(killablePlayers));
        sendToPlayer(playerHandler.getPresident().getName(), killMessage);
    }

    private void investigate() {
        logger.info("Query user to be investigated by the president");

        List<SecretHitlerPlayer> investigablePlayers = new ArrayList<>();
        for (SecretHitlerPlayer player : playerHandler.getPlayers()) {
            if (!player.isPresident() && player.isAlive()) {
                investigablePlayers.add(player);
            }
        }

        logger.info("Sending investigate to president: {}", investigablePlayers);
        sendStatus("President " + playerHandler.getPresident().getName() + "is choosing a player to be investigated");

        SecretHitlerMessage investigateMessage = buildGameMessage(GameMessageType.INVESTIGATE, playersAsString(investigablePlayers));
        sendToPlayer(playerHandler.getPresident().getName(), investigateMessage);
    }

    private void processKill(String content) {
        logger.info("Processing killed user: {}", content);
        sendStatus("President " + playerHandler.getPresident().getName() + " has killed " + content);

        SecretHitlerPlayer player = playerHandler.getPlayerByName(content);
        boolean hitlerKilled = player.isHitler();
        player.setAlive(false);

        SecretHitlerMessage killMessage = buildGameMessage(GameMessageType.KILLED, content);
        sendToAll(killMessage);
        sendToPlayer(player.getName(), killMessage);

        logger.info("Killed by the president: {}", content);

        if (hitlerKilled) {
            state = State.FINISHED;
            logger.info("LIBERAL win - Hitler is killed");
            sendStatus("LIBERAL victory - Hitler is killed");
            sendVictory(Faction.LIBERAL);
            return;
        }

        scheduleElectPresident();
    }

    private void processInvestigate(String content) {
        logger.info("Processing investigated user: {}", content);
        Faction faction = playerHandler.getPlayerByName(content).getFaction();

        if (faction.equals(Faction.HITLER)) {
            faction = Faction.FASCIST;
        }

        logger.info("Sending investigate result to president: {}", content);
        SecretHitlerMessage investigateMessage = buildGameMessage(GameMessageType.INVESTIGATE_RESULT, faction.name());
        sendToPlayer(playerHandler.getPresident().getName(), investigateMessage);
        sendStatus(content + " has been investigated by the president");

        scheduleElectPresident();
    }

    private void scheduleCheckAssets() {
        getTimer().delay(() -> checkAssets(), 5);
    }

    private void scheduleElectPresident() {
        getTimer().delay(() -> electPresident(), 5);
    }

    private void scheduleSelectPolicy() {
        getTimer().delay(() -> selectPolicy(), 5);
    }

    private void scheduleVoteGovernment() {
        getTimer().delay(() -> voteGovernment(), 5);
    }

    private String playersAsString(List<SecretHitlerPlayer> players) {
        List<String> playerNames = players.stream().map(Player::getName).collect(Collectors.toList());
        return String.join(",", playerNames);
    }

    private String policiesAsString(List<Policy> policies) {
        List<String> policyNames = policies.stream().map(Enum::toString).collect(Collectors.toList());
        return String.join(",", policyNames);
    }

    private SecretHitlerMessage buildGameMessage(GameMessageType type, String content) {
        return buildGameMessage(type, getName(), content);
    }

    private SecretHitlerMessage buildGameMessage(GameMessageType type, String sender, String content) {
        SecretHitlerMessage message = new SecretHitlerMessage();
        message.setSender(sender);
        message.setGameType(type);
        message.setContent(content);
        return message;
    }

    private void sendVictory(Faction faction) {
        SecretHitlerMessage factionMessage = buildGameMessage(GameMessageType.VICTORY, faction.name());
        sendToAll(factionMessage);
        for (SecretHitlerPlayer player : playerHandler.getPlayers()) {
            sendToPlayer(getName(), factionMessage);
        }
    }

    @Override
    public String toString() {
        return "SecretHitlerGame{" +
                "super=" + super.toString() +
                ", state=" + state +
                ", specialElection=" + specialElection +
                ", votes=" + votes +
                '}';
    }

}
