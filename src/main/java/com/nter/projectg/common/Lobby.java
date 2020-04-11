package com.nter.projectg.common;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class Lobby {

    private final Map<String, String> userSession = new HashMap<>();
    private final Map<String, String> sessionUser = new HashMap<>();

    public void add(String user, String session) {
        userSession.put(user, session);
        sessionUser.put(session, user);
    }

    public void remove(String user, String session) {
        userSession.remove(user);
        sessionUser.remove(session);
    }

    public int size() {
        return userSession.size();
    }

    public Collection<String> getUsers() {
        return userSession.keySet();
    }

    @Override
    public String toString() {
        return "Lobby{" +
                "userSession=" + userSession +
                ", sessionUser=" + sessionUser +
                '}';
    }
}
