package com.nter.projectg.games.common.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Constants {

    //TODO add game name if one is implemented
    public enum GAME_NAME {
        SECRET_HITLER
    }

    public List<String> getGames() {
        List<String> returnList = new ArrayList<>();
        for (GAME_NAME game : GAME_NAME.values()) {
            returnList.add(game.name());
        }
        returnList.add("TEST");
        return returnList;
    }

    public GAME_NAME getGameByName(String name) {
        for (GAME_NAME g : GAME_NAME.values()) {
            if (g.name().equals(name)) return g;
        }
        return null;
    }
}
