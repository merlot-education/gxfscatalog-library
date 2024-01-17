package eu.merloteducation.gxfscataloglibrary.models.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationNumber {
    @JsonProperty("@type")
    private String type;

    @JsonProperty("gax-trust-framework:local")
    private StringTypeValue local;

    @JsonProperty("gax-trust-framework:EUID")
    private StringTypeValue euid;

    @JsonProperty("gax-trust-framework:EORI")
    private StringTypeValue eori;

    @JsonProperty("gax-trust-framework:vatID")
    private StringTypeValue vatId;

    @JsonProperty("gax-trust-framework:leiCode")
    private StringTypeValue leiCode;
}
