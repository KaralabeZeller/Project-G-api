package com.nter.projectg.games.secrethitler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerHandler {
    private Map<String, SecretHitlerPlayer> playerMap;

    private SecretHitlerPlayer lastNormalPresident;
    private SecretHitlerPlayer specialPresident;

    private SecretHitlerPlayer previousPresident;
    private SecretHitlerPlayer previousChancellor;

    public PlayerHandler(List<SecretHitlerPlayer> players) {
        playerMap = players.stream().collect(Collectors.toMap(p -> p.getName(), p -> p));
    }

    public SecretHitlerPlayer getPresident() {
        return playerMap.values().stream().filter(p -> p.isPresident()).findFirst().get();
    }

    public SecretHitlerPlayer getHitler() {
        return playerMap.values().stream().filter(p -> p.isHitler()).findFirst().get();
    }

    public SecretHitlerPlayer getChancellor() {
        return playerMap.values().stream().filter(p -> p.isChancellor()).findFirst().get();
    }

    public boolean existsChancellor() {
        return playerMap.values().stream().anyMatch(p -> p.isChancellor());
    }

    public boolean existsPresident() {
        return playerMap.values().stream().anyMatch(p -> p.isPresident());
    }

    public SecretHitlerPlayer getLastNormalPresident() {
        return lastNormalPresident;
    }

    public void setLastNormalPresident(SecretHitlerPlayer lastNormalPresidentID) {
        this.lastNormalPresident = lastNormalPresidentID;
    }

    public SecretHitlerPlayer getSpecialPresident() {
        return specialPresident;
    }

    public void setSpecialPresident(SecretHitlerPlayer specialPresident) {
        this.specialPresident = specialPresident;
    }

    public int getPlayerCount() {
        return playerMap.size();
    }

    public long getAlivePlayerCount() {
        return playerMap.values().stream().filter(p -> p.isAlive()).count();
    }

    public List<SecretHitlerPlayer> getPlayers() {
        List<SecretHitlerPlayer> returnList = new ArrayList<SecretHitlerPlayer>(playerMap.values());
        return returnList;
    }

    public SecretHitlerPlayer getNextPlayer(SecretHitlerPlayer player) {
        List<SecretHitlerPlayer> players = getPlayers();
        players.add(players.get(0));
        return players.get(players.indexOf(player) + 1);
    }

    public SecretHitlerPlayer getPlayerByName(String name) {
        return playerMap.get(name);
    }


    public SecretHitlerPlayer getPreviousPresident() {
        return previousPresident;
    }

    public void setPreviousPresident(SecretHitlerPlayer previousPresident) {
        this.previousPresident = previousPresident;
    }

    public SecretHitlerPlayer getPreviousChancellor() {
        return previousChancellor;
    }

    public void setPreviousChancellor(SecretHitlerPlayer previousChancellor) {
        this.previousChancellor = previousChancellor;
    }

    @Override
    public String toString() {
        return "PlayerHandler{" +
                "playerMap=" + playerMap +
                '}';
    }
}
