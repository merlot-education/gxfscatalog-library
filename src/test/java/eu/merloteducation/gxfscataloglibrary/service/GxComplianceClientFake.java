package eu.merloteducation.gxfscataloglibrary.service;

import com.danubetech.verifiablecredentials.VerifiablePresentation;
import com.fasterxml.jackson.databind.JsonNode;

public class GxComplianceClientFake implements GxComplianceClient {
    @Override
    public JsonNode postCredentialOffer(String vcid, VerifiablePresentation body) {
        return null;
    }
}
