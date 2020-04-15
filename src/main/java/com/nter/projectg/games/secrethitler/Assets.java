package com.nter.projectg.games.secrethitler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Assets {

    private List<Constants.Faction> factions;
    public int electionTracker;
    private List<Constants.Policy> policies;
    private List<Constants.Policy> enactedPolicies;
    private List<Constants.Power> powers;
    public List<Constants.Power> activePowers;
    public int[] nonElectables;
    public HashMap<Integer, Integer> playerMap;
    public List<SecretHitlerPlayer> players;

    public Assets(List<SecretHitlerPlayer> players) {
        this.players = players;
        nonElectables = new int[2];
        electionTracker = 0;
        playerMap = new HashMap<>();

        initPlayers();
        initFactions();
        initPolicies();
        initPowers();

    }

    private void initPlayers() {
        for (int i = 0; i < players.size(); i++) {
            playerMap.put(i, 1);
        }
    }

    private void initPowers() {
        powers = new ArrayList<>();
        activePowers = new ArrayList<>();

        if (players.size() < 7) {
            powers.add(Constants.Power.POLICY_PEEK);
            powers.add(Constants.Power.EXECUTION);
            powers.add(Constants.Power.EXECUTION);

        } else {
            if (players.size() >= 9) {
                powers.add(Constants.Power.INVESTIGATE_LOYALTY);
            }

            powers.add(Constants.Power.INVESTIGATE_LOYALTY);
            powers.add(Constants.Power.SPECIAL_ELECTION);
            powers.add(Constants.Power.EXECUTION);
            powers.add(Constants.Power.EXECUTION);
        }

    }

    public void updateNotElect(int a, int b) {
        nonElectables[0] = a;
        nonElectables[1] = b;
    }

    private void initPolicies() {
        policies = new ArrayList<>(18);
        enactedPolicies = new ArrayList<>();

        for (int i = 0; i < 18; i++) {
            if (i < 11)
                policies.add(Constants.Policy.FASCIST);
            else
                policies.add(Constants.Policy.LIBERAL);
        }
        Collections.shuffle(policies);
    }

    private void initFactions() {
        factions = new ArrayList<>(players.size());

        factions.add(Constants.Faction.HITLER);
        factions.add(Constants.Faction.FASCIST);
        factions.add(Constants.Faction.LIBERAL);
        factions.add(Constants.Faction.LIBERAL);
        factions.add(Constants.Faction.LIBERAL);

        if (players.size() == 6) {
            factions.add(Constants.Faction.LIBERAL);
        }

        if (players.size() == 7) {
            factions.add(Constants.Faction.LIBERAL);
            factions.add(Constants.Faction.FASCIST);
        }

        if (players.size() == 8) {
            factions.add(Constants.Faction.LIBERAL);
            factions.add(Constants.Faction.LIBERAL);
            factions.add(Constants.Faction.FASCIST);
        }

        if (players.size() == 9) {
            factions.add(Constants.Faction.LIBERAL);
            factions.add(Constants.Faction.LIBERAL);
            factions.add(Constants.Faction.FASCIST);
            factions.add(Constants.Faction.FASCIST);
        }

        if (players.size() == 10) {
            factions.add(Constants.Faction.LIBERAL);
            factions.add(Constants.Faction.LIBERAL);
            factions.add(Constants.Faction.LIBERAL);
            factions.add(Constants.Faction.FASCIST);
            factions.add(Constants.Faction.FASCIST);
        }

        Collections.shuffle(factions);

    }

    public List<Constants.Faction> getFactions() {
        return factions;
    }

    public Constants.Policy anectTopPolicy() {
        Constants.Policy pol = policies.get(0);
        enactedPolicies.add(pol);
        policies.remove(0);
        return pol;

    }

    public List<Constants.Policy> getTopPolicies() {
        List<Constants.Policy> returnPol = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            returnPol.add(policies.get(i));
        }
        return returnPol;
    }

    public void enactPolicy(Constants.Policy policy) {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            Constants.Policy pol = policies.get(i);
            if (pol == Constants.Policy.FASCIST) {
                index = i;
                break;
            }
        }

        enactedPolicies.add(policy);
        policies.remove(index);
        Collections.shuffle(policies);
    }

    public int getPolicyCount(Constants.Policy pol) {
        int counter = 0;
        for (Constants.Policy policy : enactedPolicies) {
            if (policy == pol) {
                counter++;
            }
        }
        return counter;
    }

    public void usePower() {
        if (activePowers.size() < powers.size()) {
            activePowers.add(powers.get(activePowers.size()));
        }

    }

}
