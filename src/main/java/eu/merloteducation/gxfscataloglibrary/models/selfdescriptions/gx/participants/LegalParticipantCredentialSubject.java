package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.participants;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.PojoCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.datatypes.GxVcard;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.datatypes.NodeKindIRITypeId;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringDeserializer;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LegalParticipantCredentialSubject extends PojoCredentialSubject {

    @JsonProperty("@context")
    private Map<String, String> context = Map.of(
            "gx", "https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#",
            "vcard", "http://www.w3.org/2006/vcard/ns#",
            "xsd", "http://www.w3.org/2001/XMLSchema#"
    );

    @Getter(AccessLevel.NONE)
    public static final String TYPE = "gx:LegalParticipant";

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

    @JsonProperty("type")
    @Override
    public String getType() {
        return TYPE;
    }

    public static String getTypeNoPrefix() {
        return TYPE.replaceAll(".+:", "");
    }

}
