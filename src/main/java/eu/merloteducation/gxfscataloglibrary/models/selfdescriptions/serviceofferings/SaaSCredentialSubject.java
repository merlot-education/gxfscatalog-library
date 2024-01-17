package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.serviceofferings;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.merloteducation.gxfscataloglibrary.models.datatypes.AllowedUserCount;
import eu.merloteducation.gxfscataloglibrary.models.datatypes.StringTypeValue;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SaaSCredentialSubject extends MerlotServiceOfferingCredentialSubject {
    // inherited from merlot:MerlotServiceOfferingSaaS

    @JsonProperty("merlot:hardwareRequirements")
    private StringTypeValue hardwareRequirements;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("merlot:userCountOption")
    private List<AllowedUserCount> userCountOptions;
}
