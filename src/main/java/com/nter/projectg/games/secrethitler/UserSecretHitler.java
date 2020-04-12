package com.nter.projectg.games.secrethitler;

import com.nter.projectg.controller.GameClient;

import java.util.function.Consumer;

public class UserSecretHitler extends GameClient<SHMessage> {

    private Constants.Faction faction;

    public UserSecretHitler(String name, Consumer<SHMessage> send) {
        super(name, send);
    }

    public void setFaction(Constants.Faction faction) {
        this.faction = faction;

        SHMessage message = new SHMessage();
        message.setSender(getName());
        message.setGameMessageType(SHMessage.GameMessageType.FACTION);
        message.setContent(faction.name());
        sendMessage(message);
    }

    public Constants.Faction getFaction() {
        return faction;
    }

    @Override
    public String toString() {
        return "UserSecretHitler{" +
                "super=" + super.toString() +
                ", faction=" + faction +
                '}';
    }
}
