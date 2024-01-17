package eu.merloteducation.gxfscataloglibrary.models.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
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
