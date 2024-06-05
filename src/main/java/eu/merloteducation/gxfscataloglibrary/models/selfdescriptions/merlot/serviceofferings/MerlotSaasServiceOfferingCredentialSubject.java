package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.merlot.serviceofferings;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.PojoCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.merlot.datatypes.AllowedUserCount;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringDeserializer;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringSerializer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true, value={ "type", "@context" }, allowGetters=true)
public class MerlotSaasServiceOfferingCredentialSubject extends PojoCredentialSubject {

    @Getter(AccessLevel.NONE)
    public static final String TYPE_NAMESPACE = "merlot";
    @Getter(AccessLevel.NONE)
    public static final String TYPE_CLASS = "MerlotSaasServiceOffering";
    @Getter(AccessLevel.NONE)
    public static final String TYPE = TYPE_NAMESPACE + ":" + TYPE_CLASS;

    @Getter(AccessLevel.NONE)
    public static final Map<String, String> CONTEXT = Map.of(
            TYPE_NAMESPACE, "http://w3id.org/gaia-x/merlot#",
            "xsd", "http://www.w3.org/2001/XMLSchema#"
    );

    @JsonProperty("merlot:hardwareRequirements")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String hardwareRequirements;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("merlot:userCountOption")
    private List<AllowedUserCount> userCountOptions;

    @JsonProperty("type")
    @Override
    public String getType() {
        return TYPE;
    }

    @JsonProperty("@context")
    @Override
    public Map<String, String> getContext() {
        return CONTEXT;
    }
}
