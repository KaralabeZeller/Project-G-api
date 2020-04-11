package com.nter.projectg.games.secrethitler;

import com.nter.projectg.controller.GameClient;

public class UserSecretHitler extends GameClient{

    private Constants.Faction faction;

    public UserSecretHitler(String name) {
        super(name);

        System.out.println("SHuser: " + getName());
    }

    public void setFaction(Constants.Faction faction) {
        this.faction = faction;
        sendCommand(faction.name());
    }

    public Constants.Faction getFaction() {
        return faction;
    }


}
