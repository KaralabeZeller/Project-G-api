package com.nter.projectg.games.secrethitler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlayerHandler {
    private final Map<String, SecretHitlerPlayer> playerMap;

    private SecretHitlerPlayer lastNormalPresident;
    private SecretHitlerPlayer specialPresident;

    private SecretHitlerPlayer previousPresident;
    private SecretHitlerPlayer previousChancellor;

    public PlayerHandler(List<SecretHitlerPlayer> players) {
        playerMap = players.stream().collect(Collectors.toMap(p -> p.getName(), p -> p));
    }

    public SecretHitlerPlayer getPresident() {
        Optional<SecretHitlerPlayer> president = playerMap.values().stream().filter(p -> p.isPresident()).findFirst();
        assert president.isPresent();
        return president.get();
    }

    public SecretHitlerPlayer getHitler() {
        Optional<SecretHitlerPlayer> hitler = playerMap.values().stream().filter(p -> p.isHitler()).findFirst();
        assert hitler.isPresent();
        return hitler.get();
    }

    public SecretHitlerPlayer getChancellor() {
        Optional<SecretHitlerPlayer> chancellor = playerMap.values().stream().filter(p -> p.isChancellor()).findFirst();
        assert chancellor.isPresent();
        return chancellor.get();
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
        return new ArrayList<>(playerMap.values());
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
