package com.nter.projectg.games.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constants {

    public static List<String> games = new ArrayList<>(Arrays.asList(
            "SecretHitler",
            "Taboo"
    ));

    public static enum ClientState {
        ACCEPTED,
        READY,
        AUTH
    }


}
