package com.nter.projectg.games.secrethitler;

import com.nter.projectg.controller.GameClient;

public class UserSecretHitler {

    private Constants.Faction faction;
    public GameClient client;

    public UserSecretHitler(GameClient client) {
        this.client = client;

        System.out.println("SHuser: " + client.getName());
    }



    public void setFaction(Constants.Faction faction) {
        this.faction = faction;
        client.sendCommand(faction.name());
    }

    public Constants.Faction getFaction() {
        return faction;
    }

    public void sendCommand(String string) {
        client.sendCommand(string);

    }

    public String sendMessage(String string) {
        return client.sendQuery(string);

    }

    public void kill() {
        client.sendCommand("DSQ");
    }
}
