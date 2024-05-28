package eu.merloteducation.gxfscataloglibrary.models.credentials;

import com.danubetech.verifiablecredentials.VerifiableCredential;

public class ExtendedVerifiableCredential extends VerifiableCredential {
    @Override
    public CastableCredentialSubject getCredentialSubject() {
        return (CastableCredentialSubject) CastableCredentialSubject.getFromJsonLDObject(this);
    }
}
