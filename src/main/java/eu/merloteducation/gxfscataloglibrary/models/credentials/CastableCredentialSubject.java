package eu.merloteducation.gxfscataloglibrary.models.credentials;

import com.danubetech.verifiablecredentials.CredentialSubject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CastableCredentialSubject extends CredentialSubject {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public <T> T toPojo(Class<T> cls) {
        return objectMapper.convertValue(getJsonObject(), cls);
    }

    public static CastableCredentialSubject fromPojo(Object pojo) throws JsonProcessingException {
        return (CastableCredentialSubject) CredentialSubject.fromJson(objectMapper.writeValueAsString(pojo));
    }

}
