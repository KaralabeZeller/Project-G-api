package com.nter.projectg.model.secrethitler;

import com.nter.projectg.model.common.Message;

public class SecretHitlerMessage extends Message {

    // TODO rename (maybe)
    private GameMessageType gameMessageType;

    public SecretHitlerMessage() {
        setType(MessageType.GAME);
    }

    public enum GameMessageType {
        FACTION,
        INVESTIGATE,
        SPECIAL_ELECTION,
        NOMINEE,
        KILL,
        POLICIES,
        VOTE,
        VETO,
        POLICY
    }

    public GameMessageType getGameMessageType() {
        return gameMessageType;
    }

    public void setGameMessageType(GameMessageType gameMessageType) {
        this.gameMessageType = gameMessageType;
    }

    @Override
    public String toString() {
        return "SecretHitlerMessage{" +
                "super=" + super.toString() +
                ", gameMessageType=" + gameMessageType +
                '}';
    }

}