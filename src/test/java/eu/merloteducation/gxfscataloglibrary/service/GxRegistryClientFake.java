package eu.merloteducation.gxfscataloglibrary.service;

import com.fasterxml.jackson.databind.JsonNode;

public class GxRegistryClientFake implements GxRegistryClient {
    @Override
    public JsonNode getGxTermsAndConditions() {
        return null;
    }
}
