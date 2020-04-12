package com.nter.projectg.games.common;

import java.util.Arrays;
import java.util.List;

public class Constants {

    public static List<String> games = Arrays.asList(
            "SecretHitler",
            "Taboo"
    );

    public enum ClientState {
        ACCEPTED,
        READY,
        AUTH
    }

}
