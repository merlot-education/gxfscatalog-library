package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.serviceofferings;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.datatypes.*;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringDeserializer;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringSerializer;
import eu.merloteducation.gxfscataloglibrary.models.serialization.UriDeserializer;
import eu.merloteducation.gxfscataloglibrary.models.serialization.UriSerializer;
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
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String name;

    @NotNull
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("gax-trust-framework:termsAndConditions")
    private List<TermsAndConditions> termsAndConditions;

    @NotNull
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("gax-trust-framework:policy")
    @JsonSerialize(contentUsing = StringSerializer.class)
    @JsonDeserialize(contentUsing = StringDeserializer.class)
    private List<String> policy;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("gax-trust-framework:dataProtectionRegime")
    @JsonSerialize(contentUsing = StringSerializer.class)
    @JsonDeserialize(contentUsing = StringDeserializer.class)
    private List<String> dataProtectionRegime;

    @NotNull
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("gax-trust-framework:dataAccountExport")
    private List<DataAccountExport> dataAccountExport;

    @JsonProperty("dct:description")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String description;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("dcat:keyword")
    @JsonSerialize(contentUsing = StringSerializer.class)
    @JsonDeserialize(contentUsing = StringDeserializer.class)
    private List<String> keyword;

    @JsonProperty("gax-trust-framework:provisionType")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String provisionType;

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
    @JsonSerialize(contentUsing = UriSerializer.class)
    @JsonDeserialize(contentUsing = UriDeserializer.class)
    private List<String> trustDependsOn;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("gax-trust-framework:ServiceOfferingLocations")
    @JsonSerialize(contentUsing = StringSerializer.class)
    @JsonDeserialize(contentUsing = StringDeserializer.class)
    private List<String> serviceOfferingLocations;
}
