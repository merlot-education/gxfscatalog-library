package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.datatypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringDeserializer;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TermsAndConditions {

    @NotNull
    @JsonProperty("gax-trust-framework:content")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class) // TODO actually xsd:anyURI
    private String content;

    @NotNull
    @JsonProperty("gax-trust-framework:hash")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String hash;

    @JsonProperty("@type")
    private String type;

    @Override
    public boolean equals(Object other) {
        if (other instanceof TermsAndConditions otherTermsAndConditions){
            return content.equals((otherTermsAndConditions).getContent())
                    && hash.equals((otherTermsAndConditions).getHash());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + content.hashCode();
        result = 31 * result + hash.hashCode();
        return result;
    }

}
