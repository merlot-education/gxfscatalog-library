package eu.merloteducation.gxfscataloglibrary.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GxRegistryClientFake implements GxRegistryClient {
    @Override
    public JsonNode getGxTermsAndConditions() {
        try {
            return new ObjectMapper().readTree("""
                {
                  "version": "22.10",
                  "text": "The PARTICIPANT signing the Self-Description agrees as follows:\\n- to update its descriptions about any changes, be it technical, organizational, or legal - especially but not limited to contractual in regards to the indicated attributes present in the descriptions.\\n\\nThe keypair used to sign Verifiable Credentials will be revoked where Gaia-X Association becomes aware of any inaccurate statements in regards to the claims which result in a non-compliance with the Trust Framework and policy rules defined in the Policy Rules and Labelling Document (PRLD).\\n"
                }
                """);
        } catch (JsonProcessingException ignored) {
            return null;
        }
    }
}
