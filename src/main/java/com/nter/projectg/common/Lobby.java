package com.nter.projectg.common;

import java.util.*;

public final class Lobby {
	private static Map<String, String> userSession = new HashMap<>();
	private static Map<String, String> sessionUser = new HashMap<>();
	
	private Lobby() {

	}

	public static void addUser(String user, String session) {

		userSession.put(user, session);
		sessionUser.put(session, user);
	}

	public static void delUser(String user, String session) {
		userSession.remove(user);
		sessionUser.remove(session);
	}

	public static Collection<String> getUsers() {
		return userSession.keySet();
	}

	public static int size() {

		return userSession.size();
	}
}
