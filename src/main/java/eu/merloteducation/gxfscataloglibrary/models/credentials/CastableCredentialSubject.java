package eu.merloteducation.gxfscataloglibrary.models.credentials;

import com.danubetech.verifiablecredentials.CredentialSubject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public <T> T toPojo(Class<T> cls) {
        return objectMapper.convertValue(getJsonObject(), cls);
    }

    public static CastableCredentialSubject fromPojo(Object pojo) throws JsonProcessingException {
        return fromJson(objectMapper.writeValueAsString(pojo));
    }

}
