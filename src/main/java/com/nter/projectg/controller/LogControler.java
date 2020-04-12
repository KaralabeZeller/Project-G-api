package com.nter.projectg.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

// TODO authenticate with API key using Spring Security
@Controller
@RestController
@RequestMapping(path = "/log")
public class LogControler {

    private static final String API_KEY = "N7WxsJLRePC4ZLqy";
    private static final String LOG_FILE = "/var/log/project-g-api.log";

    private static final Logger logger = LoggerFactory.getLogger(LogControler.class);

    @GetMapping(path = "", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public ResponseEntity<String> get(@RequestParam(name = "api-key", required = false) String apiKey) throws IOException {
        if (authenticate(apiKey)) {
            try {
                logger.debug("Reading log file contents: {}", LOG_FILE);
                List<String> lines = Files.readAllLines(Paths.get(LOG_FILE));
                String contents = String.join("\n", lines);
                return ResponseEntity.ok().body(contents);
            } catch (Exception exception) {
                logger.error("Failed to read log file contents: {}", LOG_FILE, exception);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("500 Internal Server Error");
            }
        } else {
            logger.debug("Not authorized to read log file contents: invalid api-key");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("401 Not Authorized");
        }

    }

    private boolean authenticate(String apiKey) {
        return Objects.equals(API_KEY, apiKey);
    }

}
