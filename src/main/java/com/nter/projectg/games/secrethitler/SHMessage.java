package com.nter.projectg.games.secrethitler;

import com.nter.projectg.model.GMessage;

public class SHMessage extends GMessage {

    private GameMessageType gameMessageType;

    public SHMessage() {
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

}
