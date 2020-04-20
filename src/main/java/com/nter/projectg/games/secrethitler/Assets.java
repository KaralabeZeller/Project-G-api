package com.nter.projectg.games.secrethitler;

import com.nter.projectg.games.secrethitler.Constants.Faction;
import com.nter.projectg.games.secrethitler.Constants.Policy;
import com.nter.projectg.games.secrethitler.Constants.Power;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

// TODO refactor to avoid array indexing
public class Assets {

    private List<Faction> factions;
    public int electionTracker;
    private List<Policy> policies;
    private List<Policy> enactedPolicies;
    private List<Power> powers;
    public List<Power> activePowers;
    public int[] nonCandidates;
    public HashMap<Integer, Integer> playerMap;
    public List<SecretHitlerPlayer> players;

    public Assets(List<SecretHitlerPlayer> players) {
        this.players = players;
        nonCandidates = new int[2];
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
            powers.add(Power.POLICY_PEEK);
            powers.add(Power.EXECUTION);
            powers.add(Power.EXECUTION);

        } else {
            if (players.size() >= 9) {
                powers.add(Power.INVESTIGATE_LOYALTY);
            }

            powers.add(Power.INVESTIGATE_LOYALTY);
            powers.add(Power.SPECIAL_ELECTION);
            powers.add(Power.EXECUTION);
            powers.add(Power.EXECUTION);
        }

    }

    public void updateNotElect(int a, int b) {
        nonCandidates[0] = a;
        nonCandidates[1] = b;
    }

    private void initPolicies() {
        policies = new ArrayList<>(18);
        enactedPolicies = new ArrayList<>();

        for (int i = 0; i < 18; i++) {
            if (i < 11)
                policies.add(Policy.FASCIST);
            else
                policies.add(Policy.LIBERAL);
        }
        Collections.shuffle(policies);
    }

    private void initFactions() {
        factions = new ArrayList<>(players.size());

        factions.add(Faction.HITLER);
        factions.add(Faction.FASCIST);
        factions.add(Faction.LIBERAL);
        factions.add(Faction.LIBERAL);
        factions.add(Faction.LIBERAL);

        if (players.size() == 6) {
            factions.add(Faction.LIBERAL);
        }

        if (players.size() == 7) {
            factions.add(Faction.LIBERAL);
            factions.add(Faction.FASCIST);
        }

        if (players.size() == 8) {
            factions.add(Faction.LIBERAL);
            factions.add(Faction.LIBERAL);
            factions.add(Faction.FASCIST);
        }

        if (players.size() == 9) {
            factions.add(Faction.LIBERAL);
            factions.add(Faction.LIBERAL);
            factions.add(Faction.FASCIST);
            factions.add(Faction.FASCIST);
        }

        if (players.size() == 10) {
            factions.add(Faction.LIBERAL);
            factions.add(Faction.LIBERAL);
            factions.add(Faction.LIBERAL);
            factions.add(Faction.FASCIST);
            factions.add(Faction.FASCIST);
        }

        Collections.shuffle(factions);

    }

    public List<Faction> getFactions() {
        return factions;
    }

    public Policy enactTopPolicy() {
        Policy pol = policies.get(0);
        enactedPolicies.add(pol);
        policies.remove(0);
        return pol;

    }

    public List<Policy> getTopPolicies() {
        List<Policy> returnPol = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            returnPol.add(policies.get(i));
        }
        return returnPol;
    }

    public void enactPolicy(Policy policy) {
        int index = 0;
        for (int i = 0; i < 3; i++) {
            Policy pol = policies.get(i);
            if (pol == Policy.FASCIST) {
                index = i;
                break;
            }
        }

        enactedPolicies.add(policy);
        policies.remove(index);
        Collections.shuffle(policies);
    }

    public int getPolicyCount(Policy pol) {
        int counter = 0;
        for (Policy policy : enactedPolicies) {
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
