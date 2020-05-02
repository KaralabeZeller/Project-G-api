package com.nter.projectg.model.common;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.nter.projectg.model.lobby.LobbyMessage;
import com.nter.projectg.model.secrethitler.SecretHitlerMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true,
        defaultImpl = Message.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = LobbyMessage.class, name = "LOBBY"),
        @JsonSubTypes.Type(value = SecretHitlerMessage.class, name = "GAME"),
})
public class Message {

    public enum MessageType {
        LOBBY,
        START,
        STOP,
        GAME
    }

    private MessageType type;

    private String sender;

    // TODO maybe move to subtypes
    private String lobby;

    // TODO move to subtypes and make strongly typed instead of String
    private String content;

}
