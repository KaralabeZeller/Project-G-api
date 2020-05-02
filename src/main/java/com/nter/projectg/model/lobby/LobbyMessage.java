package com.nter.projectg.model.lobby;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.nter.projectg.model.common.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "lobbyType",
        visible = true,
        defaultImpl = LobbyMessage.class
)
@JsonSubTypes({
        // @JsonSubTypes.Type(value = LobbyMessage.class, name = "LOBBY"),
})
public class LobbyMessage extends Message {

    public enum LobbyMessageType {
        JOIN,
        LEAVE,
    }

    private LobbyMessageType lobbyType;

    public LobbyMessage() {
        setType(MessageType.LOBBY);
    }

}
