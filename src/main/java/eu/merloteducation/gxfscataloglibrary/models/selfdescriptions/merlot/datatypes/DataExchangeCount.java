package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.merlot.datatypes;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.merloteducation.gxfscataloglibrary.models.serialization.IntegerDeserializer;
import eu.merloteducation.gxfscataloglibrary.models.serialization.IntegerSerializer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true, value={ "@type" }, allowGetters=true)
public class DataExchangeCount {

    @Getter(AccessLevel.NONE)
    public static final String TYPE = "merlot:DataExchangeCount";

    @JsonProperty("merlot:exchangeCountUpTo")
    @JsonSerialize(using = IntegerSerializer.class)
    @JsonDeserialize(using = IntegerDeserializer.class)
    private int exchangeCountUpTo;

    @JsonProperty("@type")
    public String getType() {
        return TYPE;
    }
}
