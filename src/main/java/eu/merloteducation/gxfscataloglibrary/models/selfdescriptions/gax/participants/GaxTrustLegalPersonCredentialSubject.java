package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.participants;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.datatypes.RegistrationNumber;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.datatypes.VCard;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.SelfDescriptionCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringDeserializer;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GaxTrustLegalPersonCredentialSubject extends SelfDescriptionCredentialSubject {
    // inherited from gax-trust-framework:LegalPerson
    @JsonProperty("gax-trust-framework:legalName")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String legalName;

    @JsonProperty("gax-trust-framework:legalForm")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String legalForm;

    @JsonProperty("gax-trust-framework:description")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String description;

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
