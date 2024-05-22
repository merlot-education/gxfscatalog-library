package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SelfDescriptionVerifiableCredential {
    @JsonProperty("@context")
    private List<String> context;
    private String id;
    private List<String> type;
    private String issuer;
    private String issuanceDate;
    private SDProof proof;
    private VCCredentialSubject credentialSubject;
}
