package com.nter.projectg.games.common.util;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class Constants {

    //TODO add game name if one is implemented
    public enum GAME_NAME {
        SECRET_HITLER
    }

    public List<String> getGames() {
        return Arrays.stream(GAME_NAME.values()).map(Enum::toString).collect(Collectors.toList());
    }

    public GAME_NAME getGameByName(String name) {
        return GAME_NAME.valueOf(name);
    }

}
