package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.merlot.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.datatypes.NumberTypeValue;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.datatypes.StringTypeValue;
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
