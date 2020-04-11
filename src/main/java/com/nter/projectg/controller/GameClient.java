package com.nter.projectg.controller;

import com.nter.projectg.games.common.Constants;

public abstract class GameClient {

    private volatile Constants.ClientState state;
    private String name;

    public GameClient(String name) {
        this.name = name;
        this.state = Constants.ClientState.ACCEPTED;
    }

    private void setState(Constants.ClientState auth) {
        state = Constants.ClientState.AUTH;
    }

    public Constants.ClientState getState() {
        return state;
    }

    public String getName() {
        return name;

    }

    public void sendCommand(String name) {
        //TODO
    }

    public String sendQuery(String string) {
        //TODO
        return null;
    }
}

