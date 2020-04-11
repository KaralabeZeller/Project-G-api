package com.nter.projectg.games.secrethitler;

import com.nter.projectg.model.GMessage;

public class SHMessage extends GMessage {

    public enum MessageType {
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
}
