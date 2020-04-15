package com.nter.projectg.games.secrethitler;

import com.nter.projectg.games.common.Player;
import com.nter.projectg.games.secrethitler.Constants.Faction;
import com.nter.projectg.model.secrethitler.SecretHitlerMessage;
import com.nter.projectg.model.secrethitler.SecretHitlerMessage.GameMessageType;

import java.util.function.Consumer;

public class SecretHitlerPlayer extends Player<SecretHitlerMessage> {

    private Faction faction;

    public SecretHitlerPlayer(String name, Consumer<SecretHitlerMessage> send) {
        super(name, send);
    }

    public Faction getFaction() {
        return faction;
    }

    public void setFaction(Faction faction) {
        this.faction = faction;
    }

    public void sendCommand(GameMessageType type, String content) {
        SecretHitlerMessage message = new SecretHitlerMessage();
        message.setSender(getName());
        message.setGameType(type);
        message.setContent(content);
        send(message);
    }

    @Override
    public String toString() {
        return "SecretHitlerPlayer{" +
                "super=" + super.toString() +
                ", faction=" + faction +
                '}';
    }

}
