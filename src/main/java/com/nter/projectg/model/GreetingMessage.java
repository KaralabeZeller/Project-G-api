package com.nter.projectg.model;

public class GreetingMessage {

    private String content;

    public GreetingMessage() {
    }

    public GreetingMessage(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Greeting{" +
                "content='" + content + '\'' +
                '}';
    }

}
