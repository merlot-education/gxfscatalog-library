package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnknownCredentialSubject extends PojoCredentialSubject {
    // left empty as we cannot assume any fields
}
