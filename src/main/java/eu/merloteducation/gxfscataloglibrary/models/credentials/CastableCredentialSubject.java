package eu.merloteducation.gxfscataloglibrary.models.credentials;

import com.danubetech.verifiablecredentials.CredentialSubject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.PojoCredentialSubject;
import foundation.identity.jsonld.JsonLDObject;

import java.util.Map;

public class CastableCredentialSubject extends CredentialSubject {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @JsonCreator
    public CastableCredentialSubject() {
    }

    protected CastableCredentialSubject(Map<String, Object> stringObjectMap) {
        super(stringObjectMap);
    }

    public static CastableCredentialSubject fromJson(String json) {
        return new CastableCredentialSubject(JsonLDObject.readJson(json));
    }

    public static CastableCredentialSubject fromMap(Map<String, Object> jsonObject) {
        return new CastableCredentialSubject(jsonObject);
    }

    public static CastableCredentialSubject getFromJsonLDObject(JsonLDObject jsonLdObject) {
        return JsonLDObject.getFromJsonLDObject(CastableCredentialSubject.class, jsonLdObject);
    }

    /**
     * Method to cast a generic CastableCredentialSubject to a given POJO class.
     *
     * @param cls class to cast the CastableCredentialSubject to
     * @return POJO form of the CastableCredentialSubject
     * @param <T> type to cast the CastableCredentialSubject, must extend PojoCredentialSubject
     */
    public <T extends PojoCredentialSubject> T toPojo(Class<T> cls) {
        return objectMapper.convertValue(getJsonObject(), cls);
    }

    /**
     * Method to create a generic CastableCredentialSubject from a given PojoCredentialSubject, e.g. for
     * storing it in a catalog.
     *
     * @param pojo POJO to cast from
     * @return corresponding CastableCredentialSubject
     * @throws JsonProcessingException thrown if the given POJO cannot be converted to JSON form for casting
     */
    public static CastableCredentialSubject fromPojo(PojoCredentialSubject pojo) throws JsonProcessingException {
        return fromJson(objectMapper.writeValueAsString(pojo));
    }

}
