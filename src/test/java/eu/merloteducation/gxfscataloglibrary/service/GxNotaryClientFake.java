package eu.merloteducation.gxfscataloglibrary.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;

public class GxNotaryClientFake implements GxNotaryClient {
    @Override
    public JsonNode postRegistrationNumber(String vcid, JsonNode body) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return switch (body.get("id").textValue()) {
                case "valid" -> mapper.readTree("""
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
        } catch (JsonProcessingException ignored) {
            return null;
        }

    }
}
