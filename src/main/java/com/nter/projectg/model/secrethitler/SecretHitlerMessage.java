package com.nter.projectg.model.secrethitler;

import com.nter.projectg.model.common.Message;

public class SecretHitlerMessage extends Message {

    private GameMessageType gameType;

    public SecretHitlerMessage() {
        setType(MessageType.GAME);
    }

    public enum GameMessageType {
        FACTION,
        INVESTIGATE,
        SPECIAL_ELECTION,
        CHANCELLOR,
        KILL,
        POLICIES,
        VOTE,
        VOTED,
        VETO,
        POLICY,
        PRESIDENT,
        QUERY_CHANCELLOR
    }

    public GameMessageType getGameType() {
        return gameType;
    }

    public void setGameType(GameMessageType gameType) {
        this.gameType = gameType;
    }

    @Override
    public String toString() {
        return "SecretHitlerMessage{" +
                "super=" + super.toString() +
                ", gameType=" + gameType +
                '}';
    }

}
