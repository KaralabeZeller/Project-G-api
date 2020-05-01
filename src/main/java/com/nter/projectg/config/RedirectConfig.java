package com.nter.projectg.config;

public class RedirectConfig {
    public static String getLocation() {
        return location;
    }

    public static void setLocation(String loc) {
        location = loc;
    }

    private static String location = "LOCAL";
}
