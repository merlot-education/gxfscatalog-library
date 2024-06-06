package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions;

import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiablePresentation;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SelfDescriptionMeta {
    private String expirationTime;
    private ExtendedVerifiablePresentation content;
    private List<String> validators;
    private String subjectId;
    private String sdHash;
    private String id;
    private String status;
    private String issuer;
    private List<String> validatorDids;
    private String uploadDatetime;
    private String statusDatetime;
}
