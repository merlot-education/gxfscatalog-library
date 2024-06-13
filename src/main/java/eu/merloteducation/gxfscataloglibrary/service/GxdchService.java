package eu.merloteducation.gxfscataloglibrary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiableCredential;
import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiablePresentation;
import eu.merloteducation.gxfscataloglibrary.models.exception.ClearingHouseException;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.participants.GxLegalRegistrationNumberCredentialSubject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;
import java.util.UUID;

import static eu.merloteducation.gxfscataloglibrary.service.GxfsCatalogService.URN_UUID_PREFIX;

@Service
@Slf4j
public class GxdchService {

    private final Map<String, GxComplianceClient> gxComplianceClients;
    private final Map<String, GxRegistryClient> gxRegistryClients;
    private final Map<String, GxNotaryClient> gxNotaryClients;

    private final ObjectMapper objectMapper;

    private final int maxRetries;
    private final int retryDelay;

    private static final String ATTEMPT_MESSAGE = "Attempt #{}/{}";

    public GxdchService(@Autowired Map<String, GxComplianceClient> gxComplianceClients,
                        @Autowired Map<String, GxRegistryClient> gxRegistryClients,
                        @Autowired Map<String, GxNotaryClient> gxNotaryClients,
                        @Autowired ObjectMapper objectMapper,
                        @Value("${gxdch-services.max-retries:#{0}}") int maxRetries,
                        @Value("${gxdch-services.retry-delay:#{1000}}") int retryDelay) {
        this.gxComplianceClients = gxComplianceClients;
        this.gxRegistryClients = gxRegistryClients;
        this.gxNotaryClients = gxNotaryClients;
        this.objectMapper = objectMapper;
        this.maxRetries = maxRetries;
        this.retryDelay = retryDelay;
    }

    public ExtendedVerifiableCredential checkCompliance(ExtendedVerifiablePresentation vp) throws ClearingHouseException {
        // go through compliance service uris
        // -> try one uri, then if timeout occurs (an exception is thrown) try next uri
        ClearingHouseException clearingHouseException = null;
        for (Map.Entry<String, GxComplianceClient> clientEntry : gxComplianceClients.entrySet()) {
            int retries = 0;
            while (retries <= maxRetries) {
                ExtendedVerifiableCredential vc = null;
                try {
                    vc = checkCompliance(vp, clientEntry, retries+1);
                } catch (ClearingHouseException e) {
                    clearingHouseException = e;
                }
                if (vc != null) {
                    return vc;
                }
                retries += 1;
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (clearingHouseException != null) {
            throw clearingHouseException;
        }

        return null;
    }

    private ExtendedVerifiableCredential checkCompliance(ExtendedVerifiablePresentation vp,
                                                         Map.Entry<String, GxComplianceClient> clientEntry,
                                                         int attempt) throws ClearingHouseException {
        log.info("Checking compliance with Compliance Service {}", clientEntry.getKey());
        log.info("VP: {}", vp);
        log.info(ATTEMPT_MESSAGE, attempt, maxRetries+1);
        try {
            return clientEntry.getValue().postCredentialOffer(URN_UUID_PREFIX + UUID.randomUUID(), vp);
        } catch (WebClientResponseException e) {
            log.info("Failed to check compliance at Compliance Service {}: {} {}",
                    clientEntry.getKey(), e.getStatusCode(), e.getResponseBodyAsString());
            handleComplianceErrorResponse(e);
        } catch (Exception e) {
            log.info("Failed to check compliance at Compliance Service {}: {}", clientEntry.getKey(), e.getMessage());
        }
        return null;
    }

    public JsonNode getGxTnCs() {
        // go through registry service uris
        // -> try one uri, then if timeout occurs (an exception is thrown) try next uri
        for (Map.Entry<String, GxRegistryClient> clientEntry : gxRegistryClients.entrySet()) {
            int retries = 0;
            while (retries <= maxRetries) {
                JsonNode tnc = getGxTnCs(clientEntry, retries+1);
                if (tnc != null) {
                    return tnc;
                }
                retries += 1;
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }

        }

        return null;
    }

    private JsonNode getGxTnCs(Map.Entry<String, GxRegistryClient> clientEntry, int attempt) {
        log.info("Retrieving Gaia-X TnC at Registry {}", clientEntry.getKey());
        log.info(ATTEMPT_MESSAGE, attempt, maxRetries+1);
        try {
            return clientEntry.getValue().getGxTermsAndConditions();
        } catch (WebClientResponseException e) {
            log.info("Failed to retrieve Gaia-X TnC at Registry {}: {} {}",
                    clientEntry.getKey(), e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.info("Failed to retrieve Gaia-X TnC at Registry {}: {}", clientEntry.getKey(), e.getMessage());
        }
        return null;
    }

    public ExtendedVerifiableCredential verifyRegistrationNumber(GxLegalRegistrationNumberCredentialSubject registrationNumber) throws ClearingHouseException {
        // go through notary service uris
        // -> try one uri, then if timeout occurs (an exception is thrown) try next uri

        ClearingHouseException clearingHouseException = null;

        for (Map.Entry<String, GxNotaryClient> clientEntry : gxNotaryClients.entrySet()) {
            int retries = 0;
            while (retries <= maxRetries) {
                ExtendedVerifiableCredential vc = null;
                try {
                    vc = verifyRegistrationNumber(registrationNumber, clientEntry, retries+1);
                } catch (ClearingHouseException e) {
                    clearingHouseException = e;
                }
                if (vc != null) {
                    return vc;
                }
                retries += 1;
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (clearingHouseException != null) {
            throw clearingHouseException;
        }

        return null;
    }

    private ExtendedVerifiableCredential verifyRegistrationNumber(
            GxLegalRegistrationNumberCredentialSubject registrationNumber,
            Map.Entry<String, GxNotaryClient> clientEntry,
            int attempt) throws ClearingHouseException {
        log.info("Verifying registration number at Notary {}", clientEntry.getKey());
        log.debug("Registration number: {}", registrationNumber);
        log.info(ATTEMPT_MESSAGE, attempt, maxRetries+1);
        try {
            return clientEntry.getValue().postRegistrationNumber(URN_UUID_PREFIX + UUID.randomUUID(),
                    registrationNumber);
        } catch (WebClientResponseException e) {
            log.info("Failed to verify registration number at Notary {}: {} {}",
                    clientEntry.getKey(), e.getStatusCode(), e.getResponseBodyAsString());
            handleNotaryErrorResponse(e);
        } catch (Exception e) {
            log.info("Failed to verify registration number at Notary {}: {}",
                    clientEntry.getKey(), e.getMessage());
        }
        return null;
    }

    private void handleComplianceErrorResponse(WebClientResponseException e)
            throws ClearingHouseException {
        JsonNode errorResponse = null;
        try {
            errorResponse = objectMapper.readTree(e.getResponseBodyAsString());
        } catch (Exception ignored) {
            // unknown error
        }
        throw new ClearingHouseException(errorResponse == null
                ? "Unknown error"
                : errorResponse.get("message").asText());
    }

    private void handleNotaryErrorResponse(WebClientResponseException e)
            throws ClearingHouseException {
        throw new ClearingHouseException(e.getResponseBodyAsString());
    }


}
