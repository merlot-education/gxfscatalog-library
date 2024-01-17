package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.participants;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.merloteducation.gxfscataloglibrary.models.datatypes.StringTypeValue;
import eu.merloteducation.gxfscataloglibrary.models.datatypes.TermsAndConditions;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerlotOrganizationCredentialSubject extends GaxTrustLegalPersonCredentialSubject {
    // inherited from merlot:MerlotOrganization
    @JsonProperty("merlot:orgaName")
    @NotNull
    private StringTypeValue orgaName;

    @JsonProperty("merlot:merlotId")
    @NotNull
    private StringTypeValue merlotId;

    @JsonProperty("merlot:termsAndConditions")
    @NotNull
    private TermsAndConditions termsAndConditions;

    // TODO remove again
    @JsonProperty("merlot:mailAddress")
    @NotNull
    private StringTypeValue mailAddress;

}
