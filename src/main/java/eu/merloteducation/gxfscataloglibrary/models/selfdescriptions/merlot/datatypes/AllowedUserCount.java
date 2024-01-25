package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.merlot.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.merloteducation.gxfscataloglibrary.models.serialization.IntegerDeserializer;
import eu.merloteducation.gxfscataloglibrary.models.serialization.IntegerSerializer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AllowedUserCount {
    @JsonProperty("@type")
    private String type;

    @JsonProperty("merlot:userCountUpTo")
    @JsonSerialize(using = IntegerSerializer.class)
    @JsonDeserialize(using = IntegerDeserializer.class)
    private int userCountUpTo;
}
