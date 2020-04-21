package com.nter.projectg.games.secrethitler;

import com.nter.projectg.games.common.Player;
import com.nter.projectg.games.secrethitler.Constants.Faction;
import com.nter.projectg.model.secrethitler.SecretHitlerMessage;

import java.util.function.Consumer;

public class SecretHitlerPlayer extends Player<SecretHitlerMessage> {

    private Faction faction;
    private boolean president;
    private boolean chancellor;
    private boolean alive;

    public SecretHitlerPlayer(String name, Consumer<SecretHitlerMessage> send) {
        super(name, send);
        setPresident(false);
        setChancellor(false);
        setAlive(true);
    }

    public Faction getFaction() {
        return faction;
    }

    public void setFaction(Faction faction) {
        this.faction = faction;
    }

    public boolean isPresident() {
        return president;
    }

    public void setPresident(boolean president) {
        this.president = president;
    }

    public boolean isChancellor() {
        return chancellor;
    }

    public void setChancellor(boolean chancellor) {
        this.chancellor = chancellor;
    }

    public boolean isHitler() {
        return faction.equals(Faction.HITLER) ? true: false;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    @Override
    public String toString() {
        return "SecretHitlerPlayer{" +
                "faction=" + faction +
                ", president=" + president +
                ", chancellor=" + chancellor +
                ", alive=" + alive +
                '}';
    }
}
