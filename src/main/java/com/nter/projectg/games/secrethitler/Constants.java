package com.nter.projectg.games.secrethitler;

public class Constants {

    private Constants() {
    }

    public enum Faction {
        HITLER,
        LIBERAL,
        FASCIST
    }

    public enum Policy {
        LIBERAL,
        FASCIST
    }

    public enum Power {
        INVESTIGATE_LOYALTY,
        SPECIAL_ELECTION,
        POLICY_PEEK,
        EXECUTION
    }

    public enum State {
        ELECTION,
        NOMINATION,
        VOTE,
        ENACTMENT,
        FINISHED
    }

}
