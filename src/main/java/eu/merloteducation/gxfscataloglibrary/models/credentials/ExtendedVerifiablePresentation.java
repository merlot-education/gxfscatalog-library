package eu.merloteducation.gxfscataloglibrary.models.credentials;

import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.danubetech.verifiablecredentials.VerifiablePresentation;
import com.fasterxml.jackson.annotation.JsonCreator;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.PojoCredentialSubject;
import foundation.identity.jsonld.JsonLDObject;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExtendedVerifiablePresentation extends VerifiablePresentation {

    @JsonCreator
    public ExtendedVerifiablePresentation() {
    }

    protected ExtendedVerifiablePresentation(Map<String, Object> jsonObject) {
        super(jsonObject);
    }

    private ExtendedVerifiableCredential getExtendedVerifiableCredentialFromObject(Object o) {
        if (o instanceof Map map) {
            return ExtendedVerifiableCredential.fromMap(map);
        } else if (o instanceof ExtendedVerifiableCredential evc){
            return evc;
        } else if (o instanceof VerifiableCredential vc) {
            return ExtendedVerifiableCredential.fromMap(vc.getJsonObject());
        }
        return null;
    }

    /**
     * Extends the VerifiablePresentation base class to allow for multiple VCs in a single VP.
     * Retrieves the list of credentials in the VP object.
     *
     * @return list of VCs
     */
    public List<ExtendedVerifiableCredential> getVerifiableCredentials() {
        // get credential object
        Object credentials = getJsonObject().get(VerifiableCredential.DEFAULT_JSONLD_PREDICATE);
        if (credentials == null) {
            return Collections.emptyList();
        }

        // check if we have multiple credentials
        if (credentials instanceof List<?> credentialList) {
            return credentialList.stream()
                    .map(this::getExtendedVerifiableCredentialFromObject)
                    .filter(Objects::nonNull)
                    .toList();
        }
        // otherwise we wrap a single element into a list
        ExtendedVerifiableCredential singleCred = getExtendedVerifiableCredentialFromObject(credentials);
        if (singleCred != null) {
            return List.of(singleCred);
        }
        return Collections.emptyList();
    }

    /**
     * Extends the VerifiablePresentation base class to allow for multiple VCs in a single VP.
     * Sets the list of credentials in the VP object.
     *
     * @param credentials list of credentials to set
     */
    public void setVerifiableCredentials(List<ExtendedVerifiableCredential> credentials) {
        setJsonObjectKeyValue(VerifiableCredential.DEFAULT_JSONLD_PREDICATE,
                credentials.stream().map(JsonLDObject::getJsonObject).toList());
    }

    /**
     * Allows for easy access of the credential subjects within the VP in the form of plain old java objects (POJO)
     * for convenience. The method will check all VCs within the VP for their CS and return them as POJO if
     * they match the given type.
     *
     * @param type type class to search for
     * @return list of pojo credential subjects
     * @param <T> type of the class to search for must extend pojo credential subjects and have a TYPE attribute
     */
    public <T extends PojoCredentialSubject> List<T> findAllCredentialSubjectByType(Class<T> type) {
        String typeString;
        try {
            typeString = (String) type.getField("TYPE").get(null);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            return Collections.emptyList();
        }

        return getVerifiableCredentials().stream()
                .map(ExtendedVerifiableCredential::getCredentialSubject)
                .filter(cs -> cs.getType().equals(typeString))
                .map(cs -> cs.toPojo(type)).toList();
    }

    /**
     * Allows for easy access of the credential subjects within the VP in the form of plain old java objects (POJO)
     * for convenience. The method will check all VCs within the VP for their CS and return the first match
     * of the given type as POJO.
     *
     * @param type type class to search for
     * @return credential subject as pojo or null if it does not exist within the VP
     * @param <T> type of the class to search for must extend pojo credential subjects and have a TYPE attribute
     */
    public <T extends PojoCredentialSubject> T findFirstCredentialSubjectByType(Class<T> type) {
        List<T> pojoCredentialSubjects = findAllCredentialSubjectByType(type);
        if (pojoCredentialSubjects.isEmpty()) {
            return null;
        }
        return pojoCredentialSubjects.get(0);
    }

    public static ExtendedVerifiablePresentation fromMap(Map<String, Object> jsonObject) {
        return new ExtendedVerifiablePresentation(jsonObject);
    }
}
