package eu.merloteducation.gxfscataloglibrary.models.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Runtime {

    @JsonProperty("@type")
    private String type;

    @JsonProperty("merlot:runtimeCount")
    private NumberTypeValue runtimeCount;

    @JsonProperty("merlot:runtimeMeasurement")
    private StringTypeValue runtimeMeasurement;

}
