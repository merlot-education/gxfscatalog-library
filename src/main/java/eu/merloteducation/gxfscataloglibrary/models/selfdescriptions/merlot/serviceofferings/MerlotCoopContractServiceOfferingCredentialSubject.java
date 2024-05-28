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
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerlotCoopContractServiceOfferingCredentialSubject extends PojoCredentialSubject {
    // does not have any special fields on its own, inherits everything from general offering

    @JsonProperty("@context")
    private Map<String, String> context = Map.of(
            "merlot", "http://w3id.org/gaia-x/merlot#",
            "xsd", "http://www.w3.org/2001/XMLSchema#"
    );

    @Getter(AccessLevel.NONE)
    public static final String TYPE = "merlot:MerlotCoopContractServiceOffering";

    @JsonProperty("type")
    @Override
    public String getType() {
        return TYPE;
    }

    public static String getTypeNoPrefix() {
        return TYPE.replaceAll(".+:", "");
    }

}
