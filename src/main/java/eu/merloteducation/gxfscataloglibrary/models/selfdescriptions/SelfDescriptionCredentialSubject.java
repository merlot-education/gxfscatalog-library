package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.participants.GaxTrustLegalPersonCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.participants.MerlotOrganizationCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.serviceofferings.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, visible = true, defaultImpl = UnknownCredentialSubject.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = GaxTrustLegalPersonCredentialSubject.class, name = "gax-trust-framework:LegalPerson"),
        @JsonSubTypes.Type(value = MerlotOrganizationCredentialSubject.class, name = "merlot:MerlotOrganization"),

        @JsonSubTypes.Type(value = GaxCoreServiceOfferingCredentialSubject.class, name = "gax-core:ServiceOffering"),
        @JsonSubTypes.Type(value = GaxTrustServiceOfferingCredentialSubject.class, name = "gax-trust-framework:ServiceOffering"),
        @JsonSubTypes.Type(value = DataDeliveryCredentialSubject.class, name = "merlot:MerlotServiceOfferingDataDelivery"),
        @JsonSubTypes.Type(value = SaaSCredentialSubject.class, name = "merlot:MerlotServiceOfferingSaaS"),
        @JsonSubTypes.Type(value = CooperationCredentialSubject.class, name = "merlot:MerlotServiceOfferingCooperation")
})
public class SelfDescriptionCredentialSubject {
    // base fields
    @JsonProperty("@id")
    private String id;

    @JsonProperty("@type")
    private String type;

    @JsonProperty("@context")
    private Map<String, String> context;
}
