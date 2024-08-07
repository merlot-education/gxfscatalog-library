/*
 *  Copyright 2024 Dataport. All rights reserved. Developed as part of the MERLOT project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package eu.merloteducation.gxfscataloglibrary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiableCredential;
import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiablePresentation;
import eu.merloteducation.gxfscataloglibrary.models.exception.ClearingHouseException;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.participants.GxLegalRegistrationNumberCredentialSubject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;
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

    public GxdchService(@Autowired Map<String, GxComplianceClient> gxComplianceClients,
                        @Autowired Map<String, GxRegistryClient> gxRegistryClients,
                        @Autowired Map<String, GxNotaryClient> gxNotaryClients,
                        @Autowired ObjectMapper objectMapper) {
        this.gxComplianceClients = gxComplianceClients;
        this.gxRegistryClients = gxRegistryClients;
        this.gxNotaryClients = gxNotaryClients;
        this.objectMapper = objectMapper;
    }

    public ExtendedVerifiableCredential checkCompliance(ExtendedVerifiablePresentation vp) throws ClearingHouseException {
        // go through compliance service uris
        // -> try one uri, then if timeout occurs (an exception is thrown) try next uri
        List<ClearingHouseException> encounteredExceptions = new ArrayList<>();

        for (Map.Entry<String, GxComplianceClient> clientEntry : gxComplianceClients.entrySet()) {
            ExtendedVerifiableCredential vc = null;
            try {
                vc = checkCompliance(vp, clientEntry);
            } catch (ClearingHouseException e) {
                encounteredExceptions.add(e);
            }
            if (vc != null) {
                return vc;
            }
        }

        if (!encounteredExceptions.isEmpty()) {
            // currently we only consider the last exception
            throw encounteredExceptions.get(encounteredExceptions.size() - 1);
        }

        return null;
    }

    private ExtendedVerifiableCredential checkCompliance(ExtendedVerifiablePresentation vp,
                                                         Map.Entry<String, GxComplianceClient> clientEntry) throws ClearingHouseException {
        log.info("Checking compliance with Compliance Service {}", clientEntry.getKey());
        log.info("VP: {}", vp);
        try {
            return clientEntry.getValue().postCredentialOffer(URN_UUID_PREFIX + UUID.randomUUID(), vp);
        } catch (WebClientResponseException e) {
            log.info("Failed to check compliance at Compliance Service {}: {} {}",
                    clientEntry.getKey(), e.getStatusCode(), e.getResponseBodyAsString());
            handleComplianceErrorResponse(e);
        }
        return null;
    }

    public JsonNode getGxTnCs() { // NOSONAR this does not always return the same value, regardless of sonar nagging
        // go through registry service uris
        // -> try one uri, then if timeout occurs (an exception is thrown) try next uri
        for (Map.Entry<String, GxRegistryClient> clientEntry : gxRegistryClients.entrySet()) {
            JsonNode tnc = getGxTnCs(clientEntry);
            if (tnc != null) {
                return tnc;
            }
        }

        return null;
    }

    private JsonNode getGxTnCs(Map.Entry<String, GxRegistryClient> clientEntry) {
        log.info("Retrieving Gaia-X TnC at Registry {}", clientEntry.getKey());
        try {
            return clientEntry.getValue().getGxTermsAndConditions();
        } catch (WebClientResponseException e) {
            log.info("Failed to retrieve Gaia-X TnC at Registry {}: {} {}",
                    clientEntry.getKey(), e.getStatusCode(), e.getResponseBodyAsString());
        }
        return null;
    }

    public ExtendedVerifiableCredential verifyRegistrationNumber(GxLegalRegistrationNumberCredentialSubject registrationNumber) throws ClearingHouseException {
        // go through notary service uris
        // -> try one uri, then if timeout occurs (an exception is thrown) try next uri
        List<ClearingHouseException> encounteredExceptions = new ArrayList<>();

        for (Map.Entry<String, GxNotaryClient> clientEntry : gxNotaryClients.entrySet()) {
            ExtendedVerifiableCredential vc = null;
            try {
                vc = verifyRegistrationNumber(registrationNumber, clientEntry);
            } catch (ClearingHouseException e) {
                encounteredExceptions.add(e);
            }
            if (vc != null) {
                return vc;
            }
        }

        if (!encounteredExceptions.isEmpty()) {
            // currently we only consider the last exception
            throw encounteredExceptions.get(encounteredExceptions.size() - 1);
        }

        return null;
    }

    private ExtendedVerifiableCredential verifyRegistrationNumber(
            GxLegalRegistrationNumberCredentialSubject registrationNumber,
            Map.Entry<String, GxNotaryClient> clientEntry) throws ClearingHouseException {
        log.info("Verifying registration number at Notary {}", clientEntry.getKey());
        log.debug("Registration number: {}", registrationNumber);
        try {
            return clientEntry.getValue().postRegistrationNumber(URN_UUID_PREFIX + UUID.randomUUID(),
                    registrationNumber);
        } catch (WebClientResponseException e) {
            log.info("Failed to verify registration number at Notary {}: {} {}",
                    clientEntry.getKey(), e.getStatusCode(), e.getResponseBodyAsString());
            handleNotaryErrorResponse(e);
        }
        return null;
    }

    private void handleComplianceErrorResponse(WebClientResponseException e)
            throws ClearingHouseException {
        JsonNode errorResponse;
        try {
            errorResponse = objectMapper.readTree(e.getResponseBodyAsString());
        } catch (Exception ignored) {
            throw new ClearingHouseException("Unknown error");
        }

        JsonNode errorMessage = errorResponse.get("message");
        String errorText;
        if (errorMessage.isTextual()) {
            errorText = errorMessage.asText(); // get text content
        } else if (errorMessage.isObject()) {
            String fullError = errorMessage.toString(); // get node as json string
            int start = fullError.indexOf("ERROR:");
            int end = fullError.indexOf("DETAILS:");
            errorText = fullError.substring(start, end); // only present the error message, skip details as they are not helpful
        } else {
            errorText = errorMessage.toString(); // get node as json string
        }

        throw new ClearingHouseException(errorText);
    }

    private void handleNotaryErrorResponse(WebClientResponseException e)
            throws ClearingHouseException {
        throw new ClearingHouseException(e.getResponseBodyAsString());
    }


}
