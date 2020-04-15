package com.nter.projectg.games.common;

import com.nter.projectg.games.common.Constants.ClientState;
import com.nter.projectg.model.common.Message;

import java.util.function.Consumer;

public abstract class Player<GameMessage extends Message> {

    private final String name;
    private final Consumer<GameMessage> send;

    private ClientState state;

    public Player(String name, Consumer<GameMessage> send) {
        this.name = name;
        this.send = send;

        this.state = ClientState.ACCEPTED;
    }

    public String getName() {
        return name;
    }

    public ClientState getState() {
        return state;
    }

    private void setState(ClientState state) {
        this.state = state;
    }

    protected void send(GameMessage message) {
        send.accept(message);
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                '}';
    }

}
