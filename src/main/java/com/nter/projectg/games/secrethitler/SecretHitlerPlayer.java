package com.nter.projectg.games.secrethitler;

import com.nter.projectg.games.common.Player;
import com.nter.projectg.model.secrethitler.SecretHitlerMessage;

import java.util.function.Consumer;

public class SecretHitlerPlayer extends Player<SecretHitlerMessage> {

    private Constants.Faction faction;

    public SecretHitlerPlayer(String name, Consumer<SecretHitlerMessage> send) {
        super(name, send);
    }

    public Constants.Faction getFaction() {
        return faction;
    }

    public void setFaction(Constants.Faction faction) {
        this.faction = faction;

        // Send message to session
        SecretHitlerMessage message = new SecretHitlerMessage();
        message.setSender(getName());
        message.setGameMessageType(SecretHitlerMessage.GameMessageType.FACTION);
        message.setContent(faction.name());
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
