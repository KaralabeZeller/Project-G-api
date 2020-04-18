package com.nter.projectg.games.secrethitler;

import com.nter.projectg.games.common.Game;
import com.nter.projectg.games.secrethitler.Constants.Faction;
import com.nter.projectg.games.secrethitler.Constants.State;
import com.nter.projectg.lobby.Lobby;
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

    private SecretHitlerPlayer getHitler() {
        return players.get(hitlerID);
    }

    private SecretHitlerPlayer getChancellor() {
        return players.get(chancellorID);
    }

    private SecretHitlerPlayer getPresident() {
        return players.get(presidentID);
    }

    @Override
    protected SecretHitlerPlayer createPlayer(String name) {
        return new SecretHitlerPlayer(name, sendToPlayer(name));
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

    private void electPresident() {
        logger.info("Electing president");
        checkAssets();
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

        SecretHitlerMessage presidentMessage = buildGameMessage(GameMessageType.PRESIDENT, getPresident().getName());
        sendToAll(presidentMessage);

        logger.info("Elected president: {}", getPresident());

        nominate();
    }

    private void nominate() {
        SecretHitlerPlayer president = getPresident();
        logger.info("President nominating a chancellor: {}", president);
        state = State.NOMINATION;

        List<String> nominablePlayers = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            if (alivePlayers <= 5) {
                if (i != assets.nonElectables[1] && i != presidentID) {
                    if (assets.playerMap.get(i) == 1) {
                        SecretHitlerPlayer u = players.get(i);
                        nominablePlayers.add(u.getName());
                    }
                }
            } else {
                if (i != assets.nonElectables[0] && i != assets.nonElectables[1] && i != presidentID) {
                    if (assets.playerMap.get(i) == 1) {
                        SecretHitlerPlayer u = players.get(i);
                        nominablePlayers.add(u.getName());
                    }
                }
            }
        }

        String content = String.join(",", nominablePlayers);
        logger.info("Nominable players for chancellor {}", nominablePlayers);
        SecretHitlerMessage chancellorMessage = buildGameMessage(GameMessageType.QUERY_CHANCELLOR, content);
        sendToPlayer(president.getName(), chancellorMessage);
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
        } else if (type == GameMessageType.POLICIES) {
            processPolicies(message.getSender(), message.getContent());
        } else if (type == GameMessageType.POLICY) {
            enactPolicies(message.getContent());
        } else if (type == GameMessageType.KILL) {
            preocessKill(message.getContent());
        } else if (type == GameMessageType.INVESTIGATE) {
            preocessInvestigate(message.getContent());
        } else {
            // TODO other messages
        }
    }

    private void setChancellor(String player) {
        //TODO implement - state watchers into all process functions
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

        SecretHitlerMessage chancellorMessage = buildGameMessage(GameMessageType.CHANCELLOR, getChancellor().getName());
        sendToAll(chancellorMessage);

        voteGovernment();
    }

    private void voteGovernment() {
        logger.info("Voting for government: {}", votes);
        state = State.VOTE;

        SecretHitlerMessage voteMessage = buildGameMessage(GameMessageType.VOTE, "Ja!,Nein!");
        sendToAll(voteMessage);
    }

    private void processVote(String player, String vote) {
        logger.debug("Processing vote: {} {} {}", player, vote, votes);

        votes.process(player, vote);
        if (votes.isFinished()) {
            logger.info("Finished voting: {}", votes);
            for (Map.Entry<String, String> entry : votes.getVotes().entrySet()) {
                // Send every vote to each player to display on the UI.
                SecretHitlerMessage votedMessage = buildGameMessage(GameMessageType.VOTED, entry.getKey(), entry.getValue());
                sendToAll(votedMessage);
            }

            logger.info("Processed vote: {} {} {}", player, vote, votes);

            int jaCounter = Collections.frequency(votes.getVotes().values(), "Ja!");

            if (jaCounter - 1 >= players.size() / 2) {
                logger.info("Vote result: Ja!");
                assets.electionTracker = 0;
                assets.updateNotElect(presidentID, chancellorID);
                if (assets.getPolicyCount(Constants.Policy.FASCIST) >= 3) {
                    if (chancellorID == hitlerID) {
                        logger.info("FASCIST win - Hitler is the chancellor");
                        state = State.FINISHED;
                        return;
                    }
                }
                state = State.ENACTMENT;
                selectPolicy();
            } else {
                logger.info("Votes failed!");
                assets.electionTracker++;
                //moveTracker(); TODO implement
                state = State.ELECTION;
                electPresident();
            }

              votes.clear();
        }

    }

    private void selectPolicy() {
        List<Constants.Policy> policies = assets.getTopPolicies();

        String policyString = "";
        for (int i = 0; i < policies.size(); i++) {
            policyString += policies.get(i).name() + ",";
        }
        policyString = policyString.substring(0, policyString.length() - 1);
        logger.info("Policies for the president: {}", policyString);

        SecretHitlerMessage policyMessage = buildGameMessage(GameMessageType.POLICIES, policyString);
        sendToPlayer(getPresident().getName(), policyMessage);

        //TODO add to processPolicies
        /*
        if(nominee.equals("VETO"))
        {
            String vetoed = players.get(presidentID).client.processQuery("VETO?");
            if(vetoed.equals("Ja!"))
                veto = true;

        }
        if(!veto){
            if (nominee.equals("FASCIST")) {
                assets.enactPolicy(Constants.Policy.FASCIST);
                ui.enactPolicy(Constants.Policy.FASCIST);
            } else {
                assets.enactPolicy(Constants.Policy.LIBERAL);
                ui.enactPolicy(Constants.Policy.LIBERAL);
            }
            logger.info("--Policy enacted by government: " + nominee);
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
        logger.info("Passing policies from president to chancellor: {}", policies);
        SecretHitlerMessage policyMessage = buildGameMessage(GameMessageType.POLICY, policies);
        sendToPlayer(getChancellor().getName(), policyMessage);
    }

    private void enactPolicies(String policy) {
        if (state != State.ENACTMENT) {
            logger.warn("Message received in a false state: {}", state.name());
            return;
        }
        logger.info("Passing policies from president to chancellor: {}", policy);
        SecretHitlerMessage policyMessage = buildGameMessage(GameMessageType.ENACTED_POLICY, policy);
        sendToAll(policyMessage);
        if (policy.equals("FASCIST")) {
            assets.enactPolicy(Constants.Policy.FASCIST);
        } else {
            assets.enactPolicy(Constants.Policy.LIBERAL);
        }
        state = State.ELECTION;
        electPresident();
    }

    @Override
    public void start(String user) {
        super.start(user);

        // TODO refactor to avoid array indexing
        SecretHitlerMessage hitlerMessage = buildGameMessage(GameMessageType.HITLER, getHitler().getFaction().name());

        for (SecretHitlerPlayer player : players) {
            // Send faction message to session
            SecretHitlerMessage factionMessage = buildGameMessage(GameMessageType.FACTION, player.getFaction().name());
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
        SecretHitlerMessage factionMessage = buildGameMessage(GameMessageType.FACTION, player.getFaction().name());
        sendToPlayer(player.getName(), factionMessage);

        // Send hitler message to fascists
        if (player.getFaction() == Faction.FASCIST) {
            SecretHitlerMessage hitlerMessage = buildGameMessage(GameMessageType.HITLER, getHitler().getFaction().name());
            sendToPlayer(player.getName(), hitlerMessage);
        }

        // Send president message to session
        SecretHitlerMessage presidentMessage = buildGameMessage(GameMessageType.PRESIDENT, getPresident().getName());
        sendToPlayer(player.getName(), presidentMessage);
    }
    @Override
    public void stop() {
        super.stop();
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

    private void checkAssets() {
        if (assets.electionTracker == 3) {
            logger.info("Election tracker: 3 - Top policy will be enacted");
            Constants.Policy topPolicy = assets.anectTopPolicy();
            SecretHitlerMessage policyMessage = buildGameMessage(GameMessageType.ENACTED_POLICY, topPolicy.name());
            sendToAll(policyMessage);
            logger.info("Policy ecanted: " + topPolicy.name());
            assets.electionTracker = 0;
            //moveTracker();
        }


        if (getPlayers().size() < 7) { // 5 - 6 PLAYERS
            if (assets.getPolicyCount(Constants.Policy.FASCIST) == 3) { // PEAK TOP POLICY
                if (assets.activePowers.size() == 0) {
                    List<Constants.Policy> policies = assets.getTopPolicies();
                    String policyString = "";
                    for (int i = 0; i < policies.size(); i++) {
                        policyString += policies.get(i).name() + ",";
                    }
                    policyString = policyString.substring(0, policyString.length() - 1);
                    logger.info("Sending top policies for the president to peek: " + policyString);
                    SecretHitlerMessage policyMessage = buildGameMessage(GameMessageType.TOP_POLICIES, policyString);
                    sendToPlayer(getPresident().getName(), policyMessage);
                    assets.usePower();
                }
            }
            if (assets.getPolicyCount(Constants.Policy.FASCIST) == 4 && assets.activePowers.size() == 1) { // KILL SOMEONE
                killUser();
                assets.usePower();
            }
            if (assets.getPolicyCount(Constants.Policy.FASCIST) == 5 && assets.activePowers.size() == 2) {
                killUser();
                assets.usePower();
            }
        }

        if (getPlayers().size() == 7 || getPlayers().size() == 8) { // 7 - 8 PLAYERS

            if (assets.getPolicyCount(Constants.Policy.FASCIST) == 2) { // INVESTIGATE LOYALITY
                if (assets.activePowers.size() == 0) {
                    investigate();
                    assets.usePower();
                }
            }
            if (assets.getPolicyCount(Constants.Policy.FASCIST) == 3) { // SPECIAL ELECTION
                if (assets.activePowers.size() == 1) {
                    //specialElection(); TODO
                    assets.usePower();
                }
            }
            if (assets.getPolicyCount(Constants.Policy.FASCIST) == 4) {
                if (assets.activePowers.size() == 2) {
                    killUser();
                    assets.usePower();
                }
            }
            if (assets.getPolicyCount(Constants.Policy.FASCIST) == 5) {
                if (assets.activePowers.size() == 3) {
                    killUser();
                    assets.usePower();
                }
            }
        }

        if (getPlayers().size() == 9 || getPlayers().size() == 10) { // 9 - 10 PLAYERS

            if (assets.getPolicyCount(Constants.Policy.FASCIST) == 1) { // INVESTIGATE LOYALITY
                if (assets.activePowers.size() == 0) {
                    investigate();
                    assets.usePower();
                }
            }
            if (assets.getPolicyCount(Constants.Policy.FASCIST) == 2) { // INVESTIGATE LOYALITY
                if (assets.activePowers.size() == 1) {
                    investigate();
                    assets.usePower();
                }
            }
            if (assets.getPolicyCount(Constants.Policy.FASCIST) == 3) { // SPECIAL ELECTION
                if (assets.activePowers.size() == 2) {
                    //specialElection();
                    assets.usePower();
                }
            }
            if (assets.getPolicyCount(Constants.Policy.FASCIST) == 4) {
                if (assets.activePowers.size() == 3) {
                    killUser();
                    assets.usePower();
                }
            }
            if (assets.getPolicyCount(Constants.Policy.FASCIST) == 5) {
                if (assets.activePowers.size() == 4) {
                    killUser();
                    assets.usePower();
                }
            }
        }

        if (assets.getPolicyCount(Constants.Policy.FASCIST) == 6) {
            state = State.FINISHED;
            logger.info("FASCIST victory");
            return;
        }

        if (assets.getPolicyCount(Constants.Policy.LIBERAL) == 5) {
            state = State.FINISHED;
            logger.info("LIBERAL victory");
            return;
        }
    }

    private void killUser() {
        logger.info("Query user to be killed by the president");
        String message = "";
        for (int i = 0; i < players.size(); i++) {
            if (i != presidentID && assets.playerMap.get(i) == 1) {
                SecretHitlerPlayer u = players.get(i);
                message += u.getName() + ",";
            }
        }
        message = message.substring(0, message.length() - 1);

        SecretHitlerMessage killMessage = buildGameMessage(GameMessageType.KILL, message);
        sendToPlayer(getPresident().getName(), killMessage);


    }

    private void investigate() {
        logger.info("Query user to be investigated by the president");
        String message = "";
        for (int i = 0; i < players.size(); i++) {
            if (i != presidentID && assets.playerMap.get(i) == 1) {
                SecretHitlerPlayer u = players.get(i);
                message += u.getName() + ",";
            }
        }
        message = message.substring(0, message.length() - 1);

        SecretHitlerMessage killMessage = buildGameMessage(GameMessageType.INVESTIGATE, message);
        sendToPlayer(getPresident().getName(), killMessage);


    }

    private void preocessKill(String content) {
        logger.info("Processing killed user: " + content);
        boolean hitlerKilled = false;
        for (int i = 0; i < players.size(); i++) {

            SecretHitlerPlayer user = players.get(i);
            if (user.getName().equals(content)) {
                if (i == hitlerID)
                    hitlerKilled = true;

                assets.playerMap.replace(i, 0);
                //TODO kill and disconnect player
                alivePlayers --;
                break;
            }
        }

        logger.info("Killed by the president: " + content);

        if (hitlerKilled) {
            state = State.FINISHED;
            logger.info("LIBERAL win - Hitler is killed");
            return;
        }
    }

    private void preocessInvestigate(String content) {
        logger.info("Processing investigated user: " + content);
        Faction f = null;
        for(SecretHitlerPlayer player: players) {
            if(player.getName().equals(content))
            {
                f = player.getFaction();
                if(f.equals(Faction.HITLER)) {
                    f = Faction.FASCIST;
                }
                break;
            }
        }
        SecretHitlerMessage killMessage = buildGameMessage(GameMessageType.INVESTIGATE_RESULT, f.name());
        sendToPlayer(getPresident().getName(), killMessage);
        logger.info("Sent to president: " + content);

    }

    @Override
    public String toString() {
        return "SecretHitlerGame{" +
                "players=" + players +
                ", assets=" + assets +
                ", state=" + state +
                ", votes=" + votes +
                ", hitlerID=" + hitlerID +
                ", chancellorID=" + chancellorID +
                ", presidentID=" + presidentID +
                ", lastNormalPresident=" + lastNormalPresident +
                ", alivePlayers=" + alivePlayers +
                '}';
    }

}
