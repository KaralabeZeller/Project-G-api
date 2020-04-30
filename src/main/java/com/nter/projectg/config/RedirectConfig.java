package com.nter.projectg.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


public class RedirectConfig {
    public static String getLocation() {
        return location;
    }

    public static void setLocation(String loc) {
        location = loc;
    }

    private static String location = "LOCAL";
}
