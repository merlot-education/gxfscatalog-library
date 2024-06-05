package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.merlot.serviceofferings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.PojoCredentialSubject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true, value={ "type", "@context" }, allowGetters=true)
public class MerlotCoopContractServiceOfferingCredentialSubject extends PojoCredentialSubject {
    // does not have any special fields on its own, inherits everything from general offering

    @Getter(AccessLevel.NONE)
    public static final String TYPE_NAMESPACE = "merlot";
    @Getter(AccessLevel.NONE)
    public static final String TYPE_CLASS = "MerlotCoopContractServiceOffering";
    @Getter(AccessLevel.NONE)
    public static final String TYPE = TYPE_NAMESPACE + ":" + TYPE_CLASS;

    @Getter(AccessLevel.NONE)
    public static final Map<String, String> CONTEXT = Map.of(
            TYPE_NAMESPACE, "http://w3id.org/gaia-x/merlot#",
            "xsd", "http://www.w3.org/2001/XMLSchema#"
    );

    @JsonProperty("type")
    @Override
    public String getType() {
        return TYPE;
    }

    @JsonProperty("@context")
    @Override
    public Map<String, String> getContext() {
        return CONTEXT;
    }

}
