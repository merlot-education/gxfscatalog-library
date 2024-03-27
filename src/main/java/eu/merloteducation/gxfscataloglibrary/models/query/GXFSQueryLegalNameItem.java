package eu.merloteducation.gxfscataloglibrary.models.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GXFSQueryLegalNameItem {

    @JsonProperty("p.legalName")
    private String legalName;
}
