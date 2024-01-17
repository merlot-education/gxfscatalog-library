package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.merloteducation.gxfscataloglibrary.models.datatypes.SDProof;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SelfDescriptionVerifiableCredential {
    @JsonProperty("@context")
    private List<String> context;
    @JsonProperty("@id")
    private String id;
    @JsonProperty("@type")
    private List<String> type;
    private String issuer;
    private String issuanceDate;
    private SDProof proof;
    private SelfDescriptionCredentialSubject credentialSubject;
}
