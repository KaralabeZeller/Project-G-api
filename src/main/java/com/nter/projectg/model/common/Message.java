package com.nter.projectg.model.common;

public class Message {

    private MessageType type;

    private String sender;
    private String content;

    public Message() {
    }

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE,
        START,
        GAME
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", sender='" + sender + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

}
