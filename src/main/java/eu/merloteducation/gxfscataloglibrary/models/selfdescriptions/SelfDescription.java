package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SelfDescription {

    @NotNull
    private String id;

    private List<String> type;

    @NotNull
    @JsonProperty("@context")
    private List<String> context;

    private SDProof proof;

    private List<SelfDescriptionVerifiableCredential> verifiableCredential;

}
