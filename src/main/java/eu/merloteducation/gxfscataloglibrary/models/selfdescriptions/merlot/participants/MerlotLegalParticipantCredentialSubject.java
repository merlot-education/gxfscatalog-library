package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.merlot.participants;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.PojoCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.merlot.datatypes.ParticipantTermsAndConditions;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringDeserializer;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerlotLegalParticipantCredentialSubject extends PojoCredentialSubject {

    @JsonProperty("@context")
    private Map<String, String> context = Map.of(
            "merlot", "http://w3id.org/gaia-x/merlot#",
            "xsd", "http://www.w3.org/2001/XMLSchema#"
    );

    @Getter(AccessLevel.NONE)
    public static final String TYPE = "merlot:MerlotLegalParticipant";

    @JsonProperty("merlot:legalName")
    @NotNull
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String legalName;

    @JsonProperty("merlot:legalForm")
    @NotNull
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String legalForm;

    @JsonProperty("merlot:termsAndConditions")
    @NotNull
    private ParticipantTermsAndConditions termsAndConditions;

    /*@JsonProperty("merlot:gxdchComplianceResult")
    private String gxdchComplianceResult;*/

    @JsonProperty("type")
    @Override
    public String getType() {
        return TYPE;
    }

    public static String getTypeNoPrefix() {
        return TYPE.replaceAll(".+:", "");
    }

}
