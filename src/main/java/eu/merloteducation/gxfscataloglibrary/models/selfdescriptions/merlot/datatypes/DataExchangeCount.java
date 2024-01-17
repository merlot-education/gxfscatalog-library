package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.merlot.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.datatypes.NumberTypeValue;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataExchangeCount {

    @JsonProperty("@type")
    private String type;

    @JsonProperty("merlot:exchangeCountUpTo")
    private NumberTypeValue exchangeCountUpTo;
}
