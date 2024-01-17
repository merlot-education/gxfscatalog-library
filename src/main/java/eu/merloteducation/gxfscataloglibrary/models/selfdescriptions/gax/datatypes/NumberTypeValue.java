package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NumberTypeValue {
    @NotNull
    @JsonProperty("@type")
    private String type;

    @NotNull
    @JsonProperty("@value")
    private int value;

    public NumberTypeValue(int value) {
        this.type = "xsd:integer";
        this.value = value;
    }
}
