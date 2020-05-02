package com.nter.projectg.model.lobby;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.nter.projectg.model.common.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.json.JSONObject;

@Data
@SuperBuilder
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "callType",
        visible = true,
        defaultImpl = CallMessage.class
)
@JsonSubTypes({
        // @JsonSubTypes.Type(value = CallMessage.class, name = "Call"),
})
public class CallMessage extends Message {

    public enum CallMessageType {
        CALL
    }

    private CallMessageType callType;

    private JSONObject data;

    public CallMessage() {
        setType(MessageType.CALL);
    }

}
