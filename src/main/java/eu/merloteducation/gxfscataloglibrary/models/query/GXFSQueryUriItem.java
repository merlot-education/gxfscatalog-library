package eu.merloteducation.gxfscataloglibrary.models.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GXFSQueryUriItem {
    @JsonProperty("p.uri")
    private String uri;
}
