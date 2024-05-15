package eu.merloteducation.gxfscataloglibrary.service;

import com.danubetech.verifiablecredentials.VerifiablePresentation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@Slf4j
public class GxdchService {
    private final ObjectMapper objectMapper;

    private final Map<String, GxComplianceClient> gxComplianceClients;
    private final Map<String, GxRegistryClient> gxRegistryClients;
    private final Map<String, GxNotaryClient> gxNotaryClients;

    public GxdchService(@Autowired ObjectMapper objectMapper,
                        @Autowired Map<String, GxComplianceClient> gxComplianceClients,
                        @Autowired Map<String, GxRegistryClient> gxRegistryClients,
                        @Autowired Map<String, GxNotaryClient> gxNotaryClients) {
        this.objectMapper = objectMapper;
        this.gxComplianceClients = gxComplianceClients;
        this.gxRegistryClients = gxRegistryClients;
        this.gxNotaryClients = gxNotaryClients;
    }

    public JsonNode checkCompliance(VerifiablePresentation vp) {
        // go through compliance service uris
        // -> try one uri, then if timeout occurs (an exception is thrown) try next uri
        for (Map.Entry<String, GxComplianceClient> clientEntry : gxComplianceClients.entrySet()) {
            log.info("Checking compliance with {}", clientEntry.getKey());
            try {
                return clientEntry.getValue().postCredentialOffer(null, vp);
            } catch (Exception e) {
                log.info("Failed to check compliance at Compliance Service {}: {}", clientEntry.getKey(), e.getMessage());
            }
        }

        return null;
    }

    public JsonNode getGxTnCs() {
        // go through registry service uris
        // -> try one uri, then if timeout occurs (an exception is thrown) try next uri
        for (Map.Entry<String, GxRegistryClient> clientEntry : gxRegistryClients.entrySet()) {
            log.info("Checking compliance with {}", clientEntry.getKey());
            try {
                return clientEntry.getValue().getGxTermsAndConditions();
            } catch (Exception e) {
                log.info("Failed to retrieve Gaia-X TnC at Registry {}: {}", clientEntry.getKey(), e.getMessage());
            }
        }

        return null;
    }

    public JsonNode verifyRegistrationNumber(JsonNode registrationNumber){
        // go through notary service uris
        // -> try one uri, then if timeout occurs (an exception is thrown) try next uri
        for (Map.Entry<String, GxNotaryClient> clientEntry : gxNotaryClients.entrySet()) {
            log.info("Checking compliance with {}", clientEntry.getKey());
            try {
                return clientEntry.getValue().postRegistrationNumber(null, registrationNumber);
            } catch (Exception e) {
                log.info("Failed to check at Notary {}: {}", clientEntry.getKey(), e.getMessage());
            }
        }

        return null;
    }


}
