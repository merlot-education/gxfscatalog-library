package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PojoCredentialSubject {
    // base fields
    @JsonAlias("@id")
    private String id;

    @Getter(AccessLevel.NONE)
    public static final String TYPE = "context:type";

    @JsonProperty("@context")
    private Map<String, String> context;

    @JsonProperty("type")
    public String getType() {
        return TYPE;
    }

    public static String getTypeNoPrefix() {
        return TYPE.replaceAll(".+:", "");
    }

}
