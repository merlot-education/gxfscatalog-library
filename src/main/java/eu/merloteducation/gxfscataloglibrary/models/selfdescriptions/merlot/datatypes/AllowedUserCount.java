package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.merlot.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.datatypes.NumberTypeValue;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AllowedUserCount {
    @JsonProperty("@type")
    private String type;

    @JsonProperty("merlot:userCountUpTo")
    private NumberTypeValue userCountUpTo;
}
