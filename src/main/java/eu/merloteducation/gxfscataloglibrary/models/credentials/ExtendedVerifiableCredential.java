package eu.merloteducation.gxfscataloglibrary.models.credentials;

import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;

public class ExtendedVerifiableCredential extends VerifiableCredential {

    @JsonCreator
    public ExtendedVerifiableCredential() {
    }

    protected ExtendedVerifiableCredential(Map<String, Object> map) {
        super(map);
    }

    @Override
    public CastableCredentialSubject getCredentialSubject() {
        return CastableCredentialSubject.getFromJsonLDObject(this);
    }

    public static ExtendedVerifiableCredential fromMap(Map<String, Object> map) {
        return new ExtendedVerifiableCredential(map);
    }

    public static ExtendedVerifiableCredential fromJson(String json) {
        return new ExtendedVerifiableCredential(readJson(json));
    }
}
