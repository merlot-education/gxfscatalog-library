package eu.merloteducation.gxfscataloglibrary.service;

import com.fasterxml.jackson.databind.JsonNode;
import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiableCredential;
import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiablePresentation;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.participants.GxLegalRegistrationNumberCredentialSubject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Service
@Slf4j
public class GxdchService {

    private final Map<String, GxComplianceClient> gxComplianceClients;
    private final Map<String, GxRegistryClient> gxRegistryClients;
    private final Map<String, GxNotaryClient> gxNotaryClients;

    private final int maxRetries;

    public GxdchService(@Autowired Map<String, GxComplianceClient> gxComplianceClients,
                        @Autowired Map<String, GxRegistryClient> gxRegistryClients,
                        @Autowired Map<String, GxNotaryClient> gxNotaryClients,
                        @Value("${gxfscatalog.max-retries:#{0}}") int maxRetries) {
        this.gxComplianceClients = gxComplianceClients;
        this.gxRegistryClients = gxRegistryClients;
        this.gxNotaryClients = gxNotaryClients;
        this.maxRetries = maxRetries;
    }

    public ExtendedVerifiableCredential checkCompliance(String credentialId, ExtendedVerifiablePresentation vp) {
        // go through compliance service uris
        // -> try one uri, then if timeout occurs (an exception is thrown) try next uri
        for (Map.Entry<String, GxComplianceClient> clientEntry : gxComplianceClients.entrySet()) {
            int retries = 0;
            while (retries <= maxRetries) {
                log.info("Checking compliance with Compliance Service {}", clientEntry.getKey());
                log.info("VP: {}", vp);
                log.info("Attempt #{}", retries);
                try {
                    return clientEntry.getValue().postCredentialOffer(credentialId, vp);
                } catch (WebClientResponseException e) {
                    log.info("Failed to check compliance at Compliance Service {}: {} {}",
                            clientEntry.getKey(), e.getStatusCode(), e.getResponseBodyAsString());
                } catch (Exception e) {
                    log.info("Failed to check compliance at Compliance Service {}: {}", clientEntry.getKey(), e.getMessage());
                }
                retries += 1;
            }
        }

        return null;
    }

    public JsonNode getGxTnCs() {
        // go through registry service uris
        // -> try one uri, then if timeout occurs (an exception is thrown) try next uri
        for (Map.Entry<String, GxRegistryClient> clientEntry : gxRegistryClients.entrySet()) {
            int retries = 0;
            while (retries <= maxRetries) {
                log.info("Retrieving Gaia-X TnC at Registry {}", clientEntry.getKey());
                log.info("Attempt #{}", retries);
                try {
                    return clientEntry.getValue().getGxTermsAndConditions();
                } catch (WebClientResponseException e) {
                    log.info("Failed to retrieve Gaia-X TnC at Registry {}: {} {}",
                            clientEntry.getKey(), e.getStatusCode(), e.getResponseBodyAsString());
                } catch (Exception e) {
                    log.info("Failed to retrieve Gaia-X TnC at Registry {}: {}", clientEntry.getKey(), e.getMessage());
                }
                retries += 1;
            }

        }

        return null;
    }

    public ExtendedVerifiableCredential verifyRegistrationNumber(GxLegalRegistrationNumberCredentialSubject registrationNumber) {
        // go through notary service uris
        // -> try one uri, then if timeout occurs (an exception is thrown) try next uri
        for (Map.Entry<String, GxNotaryClient> clientEntry : gxNotaryClients.entrySet()) {
            int retries = 0;
            while (retries <= maxRetries) {
                log.info("Verifying registration number at Notary {}", clientEntry.getKey());
                log.debug("Registration number: {}", registrationNumber);
                log.info("Attempt #{}", retries);
                try {
                    return clientEntry.getValue().postRegistrationNumber(registrationNumber.getId(), registrationNumber);
                } catch (WebClientResponseException e) {
                    log.info("Failed to verify registration number at Notary {}: {} {}",
                            clientEntry.getKey(), e.getStatusCode(), e.getResponseBodyAsString());
                } catch (Exception e) {
                    log.info("Failed to verify registration number at Notary {}: {}",
                            clientEntry.getKey(), e.getMessage());
                }
                retries += 1;
            }

        }

        return null;
    }


}
