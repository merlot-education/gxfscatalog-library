package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.participants.LegalParticipantCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.participants.LegalRegistrationNumberCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.serviceofferings.ServiceOfferingCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.merlot.serviceofferings.MerlotCoopContractServiceOfferingCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.merlot.serviceofferings.MerlotDataDeliveryServiceOfferingCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.merlot.serviceofferings.MerlotSaasServiceOfferingCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.merlot.serviceofferings.MerlotServiceOfferingCredentialSubject;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true, defaultImpl = UnknownCredentialSubject.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = LegalParticipantCredentialSubject.class, name = "gx:LegalParticipant"),
        @JsonSubTypes.Type(value = LegalRegistrationNumberCredentialSubject.class, name = "gx:legalRegistrationNumber"),
        @JsonSubTypes.Type(value = ServiceOfferingCredentialSubject.class, name = "gx:ServiceOffering"),
        @JsonSubTypes.Type(value = MerlotDataDeliveryServiceOfferingCredentialSubject.class, name = "merlot:MerlotDataDeliveryServiceOffering"),
        @JsonSubTypes.Type(value = MerlotSaasServiceOfferingCredentialSubject.class, name = "merlot:MerlotSaasServiceOffering"),
        @JsonSubTypes.Type(value = MerlotCoopContractServiceOfferingCredentialSubject.class, name = "merlot:MerlotCoopContractServiceOffering"),
        @JsonSubTypes.Type(value = MerlotServiceOfferingCredentialSubject.class, name = "merlot:MerlotServiceOffering")
})
public class VCCredentialSubject {
    // base fields
    private String id;

    private String type;

    @JsonProperty("@context")
    private Map<String, String> context;
}
