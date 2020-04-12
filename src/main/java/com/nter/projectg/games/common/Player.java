package com.nter.projectg.games.common;

import com.nter.projectg.model.common.Message;

import java.util.function.Consumer;

public abstract class Player<GameMessage extends Message> {

    private final Consumer<GameMessage> send;
    private final String name;

    private volatile Constants.ClientState state;

    public Player(String name, Consumer<GameMessage> send) {
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

    protected void send(GameMessage message) {
        send.accept(message);
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                "state='" + state + '\'' +
                '}';
    }

}
