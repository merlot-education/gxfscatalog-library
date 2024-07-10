/*
 *  Copyright 2023-2024 Dataport AöR
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

import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiableCredential;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.participants.GxLegalRegistrationNumberCredentialSubject;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;

public class GxNotaryClientFake implements GxNotaryClient {
    @Override
    public ExtendedVerifiableCredential postRegistrationNumber(String vcid, GxLegalRegistrationNumberCredentialSubject body) {
        return switch (body.getId()) {
            case "valid" -> ExtendedVerifiableCredential.fromJson("""
                {
                  "@context": [
                    "https://www.w3.org/2018/credentials/v1",
                    "https://w3id.org/security/suites/jws-2020/v1"
                  ],
                  "type": [
                    "VerifiableCredential"
                  ],
                  "id": "did:web:example.com:legalRegistrationNumber1",
                  "issuer": "did:web:registration.lab.gaia-x.eu:v1-staging",
                  "issuanceDate": "2024-05-16T10:00:00.347Z",
                  "credentialSubject": {
                    "@context": "https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#",
                    "type": "gx:legalRegistrationNumber",
                    "id": "did:web:gaia-x.eu:legalRegistrationNumber.json",
                    "gx:vatID": "FR79537407926",
                    "gx:vatID-countryCode": "FR"
                  },
                  "evidence": [
                    {
                      "gx:evidenceURL": "http://ec.europa.eu/taxation_customs/vies/services/checkVatService",
                      "gx:executionDate": "2024-05-16T10:00:00.346Z",
                      "gx:evidenceOf": "gx:vatID"
                    }
                  ],
                  "proof": {
                    "type": "JsonWebSignature2020",
                    "created": "2024-05-16T10:00:00.768Z",
                    "proofPurpose": "assertionMethod",
                    "verificationMethod": "did:web:registration.lab.gaia-x.eu:v1-staging#X509-JWK2020",
                    "jws": "eyJhbGciOiJQUzI1NiIsImI2NCI6ZmFsc2UsImNyaXQiOlsiYjY0Il19..NYX-HKt9bO6JD8K8j9PbUVFvYVQcCKg4uFmG9MsLdoghSdAckFwCw0o_zjWaQudraljO7cC1VzdMsKTHoI2nzgLFL-bzE9OHfoIqFoJfILfSdT82uYL1gRND3jrR5XEb5UmtRvDESx49JWrnFrFhdvy0mZYTtH7G6MluldrAtCsin8fzBO1fV-ZcIsbeBk_CbV5sanDTQFnGsbPkBxN-PRvd_kDpyw3DkROo2Gdxlgl7xeITXdGVkY97UMido4JOZ1Hra2CLcXuUk12Z2Uaefskzdas5lC9DkwKRHQWM5x32B35eqz9Q24Kuy0RDoTT3XVjNB1Q8ZR1ZulYhwptCHg"
                  }
                }
                """);
            case "invalid" -> throw new WebClientResponseException(
                    400,
                    "",
                    null,
                    "\"Error : cannot find the Registration Number in the provided VC\"".getBytes(StandardCharsets.UTF_8),
                    null
            );
            default -> null;
        };

    }
}
