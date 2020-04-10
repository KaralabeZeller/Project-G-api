package com.nter.projectg.common;

import java.util.ArrayList;
import java.util.List;

public final class Lobby {
	private static List<String> users = new ArrayList<>();
	
	private Lobby() {

	}

	public static void addUser(String user) {
		users.add(user);
	}

	public static void delUser(String user) {
		for(int i = 0; i < users.size(); i++) {
			String locUser = users.get(i);
			if(user.equals(locUser))
			{
				users.remove(i);
				break;
			}
		}
	}

	public static List<String> getUsers() {
		return users;
	}

	public static int size() {
		return users.size();
	}
}
