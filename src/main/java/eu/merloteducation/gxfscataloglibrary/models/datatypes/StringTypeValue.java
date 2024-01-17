package eu.merloteducation.gxfscataloglibrary.models.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StringTypeValue {
    @NotNull
    @JsonProperty("@type")
    private String type;
    @NotNull
    @JsonProperty("@value")
    private String value;

    public StringTypeValue(String value) {
        this.value = value;
        this.type = "xsd:string";
    }

    public StringTypeValue(String value, String type) {
        this.value = value;
        this.type = type;
    }
}
