package com.nter.projectg.controller;

import com.nter.projectg.games.common.Constants;

public class GameClient {


    private volatile Constants.ClientState state;



    private boolean connected = false;

    public GameClient() {
        this.state = Constants.ClientState.ACCEPTED;
    }

    private void setState(Constants.ClientState auth) {
        state = Constants.ClientState.AUTH;

    }

    public Constants.ClientState getState() {
        return state;
    }

    public String getName() {
        //TODO
        return null;

    }

    public void sendCommand(String name) {
        //TODO
    }

    public String sendQuery(String string) {
        //TODO
        return null;
    }
}

