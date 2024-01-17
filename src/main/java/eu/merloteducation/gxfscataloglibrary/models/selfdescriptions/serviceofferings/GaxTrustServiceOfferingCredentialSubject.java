package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.serviceofferings;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.merloteducation.gxfscataloglibrary.models.datatypes.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GaxTrustServiceOfferingCredentialSubject extends GaxCoreServiceOfferingCredentialSubject {
    // inherited from gax-trust-framework:ServiceOffering
    @NotNull
    @JsonProperty("gax-trust-framework:name")
    private StringTypeValue name;

    @NotNull
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("gax-trust-framework:termsAndConditions")
    private List<TermsAndConditions> termsAndConditions;

    @NotNull
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("gax-trust-framework:policy")
    private List<StringTypeValue> policy;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("gax-trust-framework:dataProtectionRegime")
    private List<StringTypeValue> dataProtectionRegime;

    @NotNull
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("gax-trust-framework:dataAccountExport")
    private List<DataAccountExport> dataAccountExport;

    @JsonProperty("dct:description")
    private StringTypeValue description;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("dcat:keyword")
    private List<StringTypeValue> keyword;

    @JsonProperty("gax-trust-framework:provisionType")
    private StringTypeValue provisionType;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("gax-trust-framework:endpoint")
    private List<Endpoint> endpoint;

    @NotNull
    @JsonProperty("gax-trust-framework:providedBy")
    private NodeKindIRITypeId providedBy;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("gax-trust-framework:aggregationOf")
    private List<NodeKindIRITypeId> trustAggregationOf;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("gax-trust-framework:dependsOn")
    private List<StringTypeValue> trustDependsOn;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("gax-trust-framework:ServiceOfferingLocations")
    private List<StringTypeValue> serviceOfferingLocations;
}
