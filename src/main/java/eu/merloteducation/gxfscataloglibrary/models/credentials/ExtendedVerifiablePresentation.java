package eu.merloteducation.gxfscataloglibrary.models.credentials;

import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.danubetech.verifiablecredentials.VerifiablePresentation;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.PojoCredentialSubject;

import java.util.List;
import java.util.Map;

public class ExtendedVerifiablePresentation extends VerifiablePresentation {

    public List<ExtendedVerifiableCredential> getVerifiableCredentials() {
        // get credential object
        Object credentials = getJsonObject().get(VerifiableCredential.DEFAULT_JSONLD_PREDICATE);
        if (credentials == null) {
            return null;
        }

        // check if we have multiple credentials
        if (credentials instanceof List<?> credentialList) {
            return credentialList.stream()
                    .filter(Map.class::isInstance)
                    .map(o -> ExtendedVerifiableCredential.fromMap((Map) o))
                    .toList();
        }
        // otherwise we wrap a single element into a list
        return List.of(ExtendedVerifiableCredential.fromMap((Map) credentials));
    }

    public <T extends PojoCredentialSubject> T findFirstCredentialSubjectByType(Class<T> type) {
        String typeString;
        try {
            typeString = (String) type.getField("TYPE").get(null);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            return null;
        }

        CastableCredentialSubject credentialSubject = getVerifiableCredentials().stream()
                .map(ExtendedVerifiableCredential::getCredentialSubject)
                .filter(cs -> cs.getType().equals(typeString))
                .findFirst().orElse(null);

        if (credentialSubject == null) {
            return null;
        }

        return credentialSubject.toPojo(type);
    }

    public void setVerifiableCredentials(List<ExtendedVerifiableCredential> credentials) {
        setJsonObjectKeyValue(VerifiableCredential.DEFAULT_JSONLD_PREDICATE, credentials);
    }
}
