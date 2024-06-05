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
@JsonIgnoreProperties(ignoreUnknown = true, value={ "type", "@context" }, allowGetters=true)
public class PojoCredentialSubject {
    // base fields
    @JsonAlias("@id")
    private String id;

    @Getter(AccessLevel.NONE)
    public static final String TYPE_NAMESPACE = "context";
    @Getter(AccessLevel.NONE)
    public static final String TYPE_CLASS = "type";
    @Getter(AccessLevel.NONE)
    public static final String TYPE = TYPE_NAMESPACE + ":" + TYPE_CLASS;

    @Getter(AccessLevel.NONE)
    public static final Map<String, String> CONTEXT = Map.of();

    @JsonProperty("type")
    public String getType() {
        return TYPE;
    }

    @JsonProperty("@context")
    public Map<String, String> getContext() {
        return CONTEXT;
    }

}
