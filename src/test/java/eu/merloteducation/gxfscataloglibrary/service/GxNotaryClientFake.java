package eu.merloteducation.gxfscataloglibrary.service;

import com.fasterxml.jackson.databind.JsonNode;

public class GxNotaryClientFake implements GxNotaryClient {
    @Override
    public JsonNode postRegistrationNumber(String vcid, JsonNode body) {
        return null;
    }
}
