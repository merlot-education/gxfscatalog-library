package eu.merloteducation.gxfscataloglibrary.models.datatypes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SDProof {
    private String type;
    private String created;
    private String proofPurpose;
    private String verificationMethod;
    private String jws;
}
