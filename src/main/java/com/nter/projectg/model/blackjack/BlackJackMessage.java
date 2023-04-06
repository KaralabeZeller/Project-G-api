package com.nter.projectg.model.blackjack;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.nter.projectg.model.common.Message;
import com.nter.projectg.model.secrethitler.SecretHitlerMessage;
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
        defaultImpl = BlackJackMessage.class
)
@JsonSubTypes({
        // @JsonSubTypes.Type(value = SecretHitlerMessage.class, name = "GAME"),
})
public class BlackJackMessage extends Message {

    public enum GameMessageType {
        HIT,
        STAY,
        SPLIT,
        DOUBLE,
        BET,
        DEAL
    }

    private BlackJackMessage.GameMessageType gameType;

    public BlackJackMessage() {
        setType(MessageType.GAME);
    }

}
