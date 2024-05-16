package eu.merloteducation.gxfscataloglibrary.service;

import com.danubetech.verifiablecredentials.VerifiablePresentation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;

public class GxComplianceClientFake implements GxComplianceClient {
    @Override
    public JsonNode postCredentialOffer(String vcid, VerifiablePresentation body) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return switch (body.getId().toString()) {
                case "valid" -> mapper.readTree("""
                        {
                          "@context": [
                            "https://www.w3.org/2018/credentials/v1",
                            "https://w3id.org/security/suites/jws-2020/v1",
                            "https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#"
                          ],
                          "type": [
                            "VerifiableCredential"
                          ],
                          "id": "https://compliance.lab.gaia-x.eu/v1-staging/credential-offers/8357b334-605f-4ef7-a1da-524e096bb3cc",
                          "issuer": "did:web:compliance.lab.gaia-x.eu:v1-staging",
                          "issuanceDate": "2024-05-16T09:40:50.997Z",
                          "expirationDate": "2024-08-14T09:40:50.997Z",
                          "credentialSubject": [
                            {
                              "type": "gx:compliance",
                              "id": "https://www.example.org/legalPerson.json",
                              "gx:integrity": "sha256-1f786ade5d7af93fc3435ed6bce807004fc62c4c793eeb799b0bd6c4c9013b7e",
                              "gx:integrityNormalization": "RFC8785:JCS",
                              "gx:version": "22.10",
                              "gx:type": "gx:LegalParticipant"
                            },
                            {
                              "type": "gx:compliance",
                              "id": "https://wizard.lab.gaia-x.eu/api/credentials/2d37wbGvQzbAQ84yRouh2m2vBKkN8s5AfH9Q75HZRCUQmJW7yAVSNKzjJj6gcjE2mDNDUHCichXWdMH3S2c8AaDLm3kXmf5R8DVh1NjFG5eCRJFea4yra4VNPAAH7L1cf3vZqRcG5YCr5py8YAGxgrrfSAsXqZSDE7ftWfsDCQyrzQH4jRmTSEjFdtDGi95JbGUhSzcnyKAA99JZxuEUwyDBCJSV9PYF9YLJjhjzFCb6aCanMU1SyH4owrBe1FwGp34eFsbRVwrnoGhzVUuVGijrL39V6Pufq8YM2wafumikRxXrtyQiMH3whXBW7JUHGbBE2JyC9UJpcfbN6ph9cGwpadvyjApJ1syYiEzKg3z7hKPW72e8DCgiCe94QtRpnvhmP4icMHHLcYyF2aqkG48pBZ6MQdaNPTyu1PFB751wiPk2dge42f4knFR7AfZgLG5SgcwS8mjf9ad8f6VRwNadRxGgGcpt63MwgrQBLJpJ9HYvpP6mENv1MLRjMteZGARJyGj6AKnECuoPkhSSsm4Ps3Htvr1K5pc83QCPDMph2UmaRYy6nvSteR5jXGEm5uV8pmTmEmL8c3C1wKz6168zD68Rxyys1xnQqoz?uid=b98f7a2a-b8f6-4c3c-897c-7e3ef5ee3ce0#cd8b7f721dcfbbcedd85ff54c59aaea71343c244001bf8b3f52bb5463e3ec9f6",
                              "gx:integrity": "sha256-e685406b27a68aa074ed13cbab417be2607db67cf2ebfd1e0712d706e218980a",
                              "gx:integrityNormalization": "RFC8785:JCS",
                              "gx:version": "22.10",
                              "gx:type": "gx:legalRegistrationNumber"
                            }
                          ],
                          "proof": {
                            "type": "JsonWebSignature2020",
                            "created": "2024-05-16T09:40:51.466Z",
                            "proofPurpose": "assertionMethod",
                            "verificationMethod": "did:web:compliance.lab.gaia-x.eu:v1-staging#X509-JWK2020",
                            "jws": "eyJhbGciOiJQUzI1NiIsImI2NCI6ZmFsc2UsImNyaXQiOlsiYjY0Il19..UKpCThYh7xOnv0Ka8uXyTW733b1TxeTic9Z8O9yLNhietHtTBYJbAiWRBwHHDN2n5aVKWJMjK5bQ35L_wTc_o1Tsb4HFIKdhZLOkhZwCotaKfNAOsnvdfuAVyJGCtDWznnPDY_06HWccpoDD9BtdYLrG5zJLzc2h2rTPT2MUUk10mR1gw2eaJEGmQoFC1ip4IHvAbaSUkm9IWmNvORHiMxXkO_7gM7o7Y9lCAVzf-3mu4o_QvQ9Su0en4S_cLpPXJksBtd1eD9SgJ5ohA6Eho91Wfsf5tXF-MF5yc4e1jyScxbP3I0oIjaTbZ3zPf9pCD2AtoSkSTGqh7R3Q_GR5EA"
                          }
                        }
                        """);
                case "badcert" -> throw new WebClientResponseException(
                        409,
                        "",
                        null,
                        ("""
                                {
                                  "message": "X509 certificate chain could not be resolved against registry trust anchors for VC\s""" + body.getId().toString() + """
                                  \",
                                  "error": "Conflict",
                                  "statusCode": 409
                                }
                                """).getBytes(StandardCharsets.UTF_8),
                        null
                );
                case "badsignature" -> throw new WebClientResponseException(
                        409,
                        "",
                        null,
                        ("""
                                {
                                  "message": "Could not load document for given did:web:\s\s""" + body.getVerifiableCredential().getIssuer().toString() + """
                                  \",
                                  "error": "Conflict",
                                  "statusCode": 409
                                }
                                """).getBytes(StandardCharsets.UTF_8),
                        null
                );
                case "badshape" -> throw new WebClientResponseException(
                        409,
                        "",
                        null,
                        ("""
                                {
                                  "message": "VerifiableCrdential contains a shape that is not defined in registry shapes",
                                  "error": "Conflict",
                                  "statusCode": 409
                                }
                                """).getBytes(StandardCharsets.UTF_8),
                        null
                );
                default -> null;
            };
        } catch (JsonProcessingException ignored) {
            return null;
        }

    }
}
