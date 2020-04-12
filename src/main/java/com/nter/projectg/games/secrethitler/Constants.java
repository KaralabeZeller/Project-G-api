package com.nter.projectg.games.secrethitler;

public class Constants {

    public enum Faction {
        HITLER,
        LIBERAL,
        FASCIST
    }

    public enum SHState {
        ELECTION,
        NOMINATION,
        VOTE,
        EVALUATE,
        ENACTMENT,
        FINISHED,
        DEBUG
    }

    public enum Policy {
        LIBERAL,
        FASCIST
    }

    public enum Power {
        INVESTIGATE_LOYALITY,
        SPECIAL_ELECTION,
        POLICY_PEEK,
        EXECUTION
    }

}
