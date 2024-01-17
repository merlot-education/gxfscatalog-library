package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.serviceofferings;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.datatypes.NodeKindIRITypeId;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.SelfDescriptionCredentialSubject;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GaxCoreServiceOfferingCredentialSubject extends SelfDescriptionCredentialSubject {
    // inherited from gax-core:ServiceOffering
    @NotNull
    @JsonProperty("gax-core:offeredBy")
    private NodeKindIRITypeId offeredBy;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("gax-core:aggregationOf")
    private List<NodeKindIRITypeId> coreAggregationOf;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("gax-core:dependsOn")
    private List<NodeKindIRITypeId> coreDependsOn;
}
