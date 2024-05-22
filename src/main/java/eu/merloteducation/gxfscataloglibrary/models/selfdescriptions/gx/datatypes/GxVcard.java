package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.datatypes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringDeserializer;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GxVcard {

    @JsonProperty("gx:countryCode")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    @NotNull
    private String countryCode;

    @JsonProperty("gx:countrySubdivisionCode")
    @JsonSerialize(contentUsing = StringSerializer.class)
    @JsonDeserialize(contentUsing = StringDeserializer.class)
    @NotNull
    private List<String> countrySubdivisionCode;

    @JsonProperty("vcard:street-address")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String streetAddress;

    @JsonProperty("vcard:locality")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String locality;

    @JsonProperty("vcard:postal-code")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String postalCode;
}
