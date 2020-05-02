package com.nter.projectg.model.secrethitler;

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
        property = "gameType",
        visible = true,
        defaultImpl = SecretHitlerMessage.class
)
@JsonSubTypes({
        // @JsonSubTypes.Type(value = SecretHitlerMessage.class, name = "GAME"),
})
public class SecretHitlerMessage extends Message {

    public enum GameMessageType {
        FACTION,
        HITLER,
        PRESIDENT,
        QUERY_CHANCELLOR,
        CHANCELLOR,
        VOTE,
        VOTED,
        KILL,
        KILLED,
        POLICIES,
        POLICY,
        ENACTED_POLICY,
        INVESTIGATE,
        INVESTIGATE_RESULT,
        SPECIAL_ELECTION,
        TOP_POLICIES,
        STATE,
        VICTORY,
        FELLOW_FASCIST,
        TRACKER,
        VETO,
    }

    private GameMessageType gameType;

    public SecretHitlerMessage() {
        setType(MessageType.GAME);
    }

}
