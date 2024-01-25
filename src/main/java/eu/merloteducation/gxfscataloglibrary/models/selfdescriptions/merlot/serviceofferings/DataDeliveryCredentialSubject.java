package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.merlot.serviceofferings;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.merlot.datatypes.DataExchangeCount;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringDeserializer;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataDeliveryCredentialSubject extends MerlotServiceOfferingCredentialSubject {
    // inherited from merlot:MerlotServiceOfferingDataDelivery

    @NotNull
    @JsonProperty("merlot:dataAccessType")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String dataAccessType;

    @NotNull
    @JsonProperty("merlot:dataTransferType")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String dataTransferType;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("merlot:exchangeCountOption")
    private List<DataExchangeCount> exchangeCountOptions;
}
