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
public class VCard {

    @JsonProperty("@type")
    private String type;

    @JsonProperty("vcard:country-name")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String countryName;

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
