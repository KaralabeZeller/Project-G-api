package com.nter.projectg.model.secrethitler;

import com.nter.projectg.model.common.Message;

public class SecretHitlerMessage extends Message {

    public enum GameMessageType {
        FACTION,
        HITLER,
        PRESIDENT,
        QUERY_CHANCELLOR,
        CHANCELLOR,
        VOTE,
        VOTED,
        KILL,
        KILLED,
        POLICIES,
        POLICY,
        ENACTED_POLICY,
        INVESTIGATE,
        INVESTIGATE_RESULT,
        SPECIAL_ELECTION,
        TOP_POLICIES,
        STATE,
        VETO,
    }

    private GameMessageType gameType;

    public SecretHitlerMessage() {
        setType(MessageType.GAME);
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
