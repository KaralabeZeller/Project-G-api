package com.nter.projectg.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

// TODO authenticate with API key using Spring Security
@Controller
@RestController
@RequestMapping(path = "/log")
public class LogController {

    @Value("${log-controller.api-key}")
    private String API_KEY = "";
    @Value("${log-controller.log-file}")
    private String LOG_FILE = "";
    @Value("${log-controller.log-lines}")
    private int LOG_LINES = 10000;

    private static final Logger logger = LoggerFactory.getLogger(LogController.class);

    @GetMapping(path = "", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public ResponseEntity<String> get(@RequestParam(name = "api-key", required = false) String apiKey) {
        if (authenticate(apiKey)) {
            try {
                logger.debug("Reading log file contents: {}", LOG_FILE);
                // TODO stream lines to avoid loading the entire file in memory
                List<String> lines = Files.readAllLines(Paths.get(LOG_FILE));
                List<String> tail = lines.subList(Math.max(0, lines.size() - 1 - LOG_LINES), lines.size() - 1);
                String contents = String.join(System.lineSeparator(), tail);
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
