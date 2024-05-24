package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.merlot.serviceofferings;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.VCCredentialSubject;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerlotCoopContractServiceOfferingCredentialSubject extends VCCredentialSubject {
    // does not have any special fields on its own, inherits everything from general offering

    @JsonProperty("@context")
    private Map<String, String> context = Map.of(
            "merlot", "http://w3id.org/gaia-x/merlot#",
            "xsd", "http://www.w3.org/2001/XMLSchema#"
    );

    private String type = "merlot:MerlotCoopContractServiceOffering";

}
