package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.participants;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.PojoCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringDeserializer;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringSerializer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true, value={ "type", "@context" }, allowGetters=true)
public class GxLegalRegistrationNumberCredentialSubject extends PojoCredentialSubject {

    @Getter(AccessLevel.NONE)
    public static final Map<String, String> CONTEXT = Map.of(
            "gx", "https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#",
            "xsd", "http://www.w3.org/2001/XMLSchema#"
    );

    @Getter(AccessLevel.NONE)
    public static final String TYPE = "gx:legalRegistrationNumber";

    @JsonProperty("gx:taxID")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String taxID;

    @JsonProperty("gx:EUID")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String euid;

    @JsonProperty("gx:EORI")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String eori;

    @JsonProperty("gx:vatID")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String vatID;

    @JsonProperty("gx:leiCode")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String leiCode;

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

    public static String getTypeNoPrefix() {
        return TYPE.replaceAll(".+:", "");
    }
}
