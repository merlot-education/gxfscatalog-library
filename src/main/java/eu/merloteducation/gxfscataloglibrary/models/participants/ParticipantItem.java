package eu.merloteducation.gxfscataloglibrary.models.participants;

import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiablePresentation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParticipantItem {
    private String id;
    private String name;
    private PublicKey publicKey;
    private ExtendedVerifiablePresentation selfDescription;
}
