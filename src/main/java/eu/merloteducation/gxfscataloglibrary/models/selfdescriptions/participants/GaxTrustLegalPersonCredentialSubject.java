package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.participants;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.merloteducation.gxfscataloglibrary.models.datatypes.RegistrationNumber;
import eu.merloteducation.gxfscataloglibrary.models.datatypes.StringTypeValue;
import eu.merloteducation.gxfscataloglibrary.models.datatypes.VCard;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.SelfDescriptionCredentialSubject;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GaxTrustLegalPersonCredentialSubject extends SelfDescriptionCredentialSubject {
    // inherited from gax-trust-framework:LegalPerson
    @JsonProperty("gax-trust-framework:legalName")
    private StringTypeValue legalName;

    @JsonProperty("gax-trust-framework:legalForm")
    private StringTypeValue legalForm;

    @JsonProperty("gax-trust-framework:description")
    private StringTypeValue description;

    @JsonProperty("gax-trust-framework:registrationNumber")
    @NotNull
    private RegistrationNumber registrationNumber;

    @JsonProperty("gax-trust-framework:legalAddress")
    @NotNull
    private VCard legalAddress;

    @JsonProperty("gax-trust-framework:headquarterAddress")
    @NotNull
    private VCard headquarterAddress;
}
