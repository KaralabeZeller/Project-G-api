package com.nter.projectg.controller;

import com.nter.projectg.games.common.Constants;
import com.nter.projectg.model.GMessage;

import java.util.function.Consumer;

public abstract class GameClient<Message extends GMessage> {

    private final Consumer<Message> send;

    private volatile Constants.ClientState state;
    private final String name;

    public GameClient(String name, Consumer<Message> send) {
        this.send = send;
        this.name = name;
        this.state = Constants.ClientState.ACCEPTED;
    }

    public String getName() {
        return name;
    }

    private void setState(Constants.ClientState auth) {
        state = Constants.ClientState.AUTH;
    }

    public Constants.ClientState getState() {
        return state;
    }

    protected void sendMessage(Message message) {
        send.accept(message);
    }

    @Override
    public String toString() {
        return "GameClient{" +
                "name='" + name + '\'' +
                '}';
    }

}

