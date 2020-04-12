package com.nter.projectg.games.secrethitler;

import com.nter.projectg.games.common.Player;
import com.nter.projectg.model.secrethitler.SecretHitlerMessage;

import java.util.function.Consumer;

public class SecretHitlerPlayer extends Player<SecretHitlerMessage> {

    private Constants.Faction faction;

    public SecretHitlerPlayer(String name, Consumer<SecretHitlerMessage> send) {
        super(name, send);
    }

    public void setFaction(Constants.Faction faction) {
        this.faction = faction;

        // Unicast GAME FACTION message
        SecretHitlerMessage message = new SecretHitlerMessage();
        message.setSender(getName());
        message.setGameMessageType(SecretHitlerMessage.GameMessageType.FACTION);
        message.setContent(faction.name());
        send(message);
    }

    public Constants.Faction getFaction() {
        return faction;
    }

    @Override
    public String toString() {
        return "SecretHitlerPlayer{" +
                "super=" + super.toString() +
                ", faction=" + faction +
                '}';
    }
}
