package com.nter.projectg.games.secrethitler;

public class Constants {

    public static enum Faction {
        HITLER,
        LIBERAL,
        FASCIST
    }

    public static enum SHState {
        ELECTION,
        NOMINATION,
        VOTE,
        EVALUATE,
        ENACTMENT,
        FINISHED,
        DEBUG
    }

    public static enum Policy {
        LIBERAL,
        FASCIST
    }

    public static enum Power {
        INVESTIGATE_LOYALITY,
        SPECIAL_ELECTION,
        POLICY_PEEK,
        EXECUTION
    }

}
