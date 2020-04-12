package com.nter.projectg.controller;

import com.nter.projectg.model.GreetingMessage;
import com.nter.projectg.model.HelloMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class GreetingController {

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public GreetingMessage greeting(HelloMessage message) throws Exception {
        // Fake synchronous computation
        Thread.sleep(100);

        return new GreetingMessage("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
    }

}
