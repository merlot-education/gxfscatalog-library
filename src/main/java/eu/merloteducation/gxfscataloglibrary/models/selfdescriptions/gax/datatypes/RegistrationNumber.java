package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringDeserializer;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringSerializer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationNumber {
    @JsonProperty("@type")
    private String type;

    @JsonProperty("gax-trust-framework:local")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String local;

    @JsonProperty("gax-trust-framework:EUID")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String euid;

    @JsonProperty("gax-trust-framework:EORI")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String eori;

    @JsonProperty("gax-trust-framework:vatID")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String vatId;

    @JsonProperty("gax-trust-framework:leiCode")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String leiCode;
}
