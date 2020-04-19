package com.nter.projectg.model.common;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.nter.projectg.model.secrethitler.SecretHitlerMessage;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true,
        defaultImpl = Message.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SecretHitlerMessage.class, name = "GAME"),
})
public class Message {

    public enum MessageType {
        JOIN,
        LEAVE,
        START,
        GAME,
        STOP
    }

    private MessageType type;
    private String sender;

    private String content;

    public Message() {
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
