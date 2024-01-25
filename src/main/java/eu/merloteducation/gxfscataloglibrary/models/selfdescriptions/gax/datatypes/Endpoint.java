package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.datatypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.merloteducation.gxfscataloglibrary.models.serialization.UriDeserializer;
import eu.merloteducation.gxfscataloglibrary.models.serialization.UriSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Endpoint {

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("gax-trust-framework:endPointURL")
    @JsonSerialize(using = UriSerializer.class)
    @JsonDeserialize(using = UriDeserializer.class)
    private List<String> endPointURL;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("gax-trust-framework:standardConformity")
    private List<Standard> standardConformity;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("gax-trust-framework:endpointDescription")
    @JsonSerialize(contentUsing = UriSerializer.class)
    @JsonDeserialize(contentUsing = UriDeserializer.class)
    private List<String> endpointDescription;

    @NotNull
    @JsonProperty("@type")
    private String type;
}
