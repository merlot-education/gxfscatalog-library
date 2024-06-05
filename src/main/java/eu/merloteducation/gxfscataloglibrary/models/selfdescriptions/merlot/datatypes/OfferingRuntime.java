package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.merlot.datatypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.merloteducation.gxfscataloglibrary.models.serialization.IntegerDeserializer;
import eu.merloteducation.gxfscataloglibrary.models.serialization.IntegerSerializer;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringDeserializer;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringSerializer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true, value={ "@type" }, allowGetters=true)
public class OfferingRuntime {

    @Getter(AccessLevel.NONE)
    public static final String TYPE = "merlot:Runtime";

    @JsonProperty("merlot:runtimeCount")
    @JsonSerialize(using = IntegerSerializer.class)
    @JsonDeserialize(using = IntegerDeserializer.class)
    private int runtimeCount;

    @JsonProperty("merlot:runtimeMeasurement")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String runtimeMeasurement;

    @JsonProperty("@type")
    public String getType() {
        return TYPE;
    }
}
