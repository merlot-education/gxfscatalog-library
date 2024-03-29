package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.datatypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
public class Standard {

    @JsonProperty("@type")
    private String type;

    @NotNull
    @JsonProperty("gax-trust-framework:title")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String title;

    @NotNull
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("gax-trust-framework:standardReference")
    @JsonSerialize(contentUsing = UriSerializer.class)
    @JsonDeserialize(contentUsing = UriDeserializer.class)
    private List<String> standardReference;

    @JsonProperty("gax-trust-framework:publisher")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String publisher;

}
