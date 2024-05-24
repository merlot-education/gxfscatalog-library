package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.participants;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.VCCredentialSubject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LegalRegistrationNumberCredentialSubject extends VCCredentialSubject {

    @JsonProperty("@context")
    private Map<String, String> context = Map.of(
            "gx", "https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#",
            "xsd", "http://www.w3.org/2001/XMLSchema#"
    );

    private String type = "gx:legalRegistrationNumber";

    @JsonProperty("gx:taxID")
    private String taxID;

    @JsonProperty("gx:EUID")
    private String euid;

    @JsonProperty("gx:EORI")
    private String eori;

    @JsonProperty("gx:vatID")
    private String vatID;

    @JsonProperty("gx:leiCode")
    private String leiCode;
}
