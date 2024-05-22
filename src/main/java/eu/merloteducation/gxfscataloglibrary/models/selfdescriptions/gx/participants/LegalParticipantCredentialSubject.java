package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.participants;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.VCCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.datatypes.GxVcard;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.datatypes.NodeKindIRITypeId;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringDeserializer;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LegalParticipantCredentialSubject extends VCCredentialSubject {

    // Tagus
    @NotNull
    @JsonProperty("gx:legalRegistrationNumber")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<NodeKindIRITypeId> legalRegistrationNumber; // will be gx:registrationNumber in Loire

    // Tagus
    @JsonProperty("gx:parentOrganization")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<NodeKindIRITypeId> parentOrganization; // will be gx:parentOrganizationOf in Loire

    // Tagus
    @JsonProperty("gx:subOrganization")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<NodeKindIRITypeId> subOrganization; // will be gx:subOrganizationOf in Loire

    // Loire
    @NotNull
    @JsonProperty("gx:legalAddress")
    private GxVcard legalAddress; // contains Tagus gx:countrySubdivisionCode

    // Loire
    @NotNull
    @JsonProperty("gx:headquarterAddress")
    private GxVcard headquarterAddress; // contains Tagus gx:countrySubdivisionCode

    // Loire
    @JsonProperty("gx:name")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String name;

    // Loire
    @JsonProperty("gx:description")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String description;


}
