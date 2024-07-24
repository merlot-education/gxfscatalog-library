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

import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiableCredential;
import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiablePresentation;
import eu.merloteducation.gxfscataloglibrary.models.exception.ClearingHouseException;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.participants.GxLegalRegistrationNumberCredentialSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@EnableConfigurationProperties
class GxdchServiceTests {
    @Autowired
    private GxdchService gxdchService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Map<String, GxComplianceClient> gxComplianceClients;
    @MockBean
    private Map<String, GxRegistryClient> gxRegistryClients;
    @MockBean
    private Map<String, GxNotaryClient> gxNotaryClients;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(gxdchService, "gxComplianceClients",
                Map.of("https://example.com/1", new GxComplianceClientFake(),
                        "https://example.com/2", new GxComplianceClientFake()));
        ReflectionTestUtils.setField(gxdchService, "gxRegistryClients",
                Map.of("https://example.com/1", new GxRegistryClientFake(),
                "https://example.com/2", new GxRegistryClientFake()));
        ReflectionTestUtils.setField(gxdchService, "gxNotaryClients",
                Map.of("https://example.com/1", new GxNotaryClientFake(),
                "https://example.com/2", new GxNotaryClientFake()));
    }

    @Test
    void checkComplianceSuccess() throws ClearingHouseException {
        ExtendedVerifiablePresentation vp = new ExtendedVerifiablePresentation();
        vp.setJsonObjectKeyValue("id", "valid");
        ExtendedVerifiableCredential result = gxdchService.checkCompliance(vp);
        assertNotNull(result);
    }

    @ParameterizedTest
    @CsvSource({
            "badsignature",
            "badcert",
            "badshape",
            "badsemantics"
    })
    void checkComplianceBad(String shapeName) {
        ExtendedVerifiablePresentation vp = new ExtendedVerifiablePresentation();
        vp.setVerifiableCredentials(List.of(
                ExtendedVerifiableCredential.fromMap(
                        VerifiableCredential.builder()
                                .issuer(URI.create("http://example.com")).build().getJsonObject())));
        vp.setJsonObjectKeyValue("id", shapeName);
        assertThrows(ClearingHouseException.class, () -> gxdchService.checkCompliance(vp));
    }

    @Test
    void getTncSuccess() {
        JsonNode result = gxdchService.getGxTnCs();
        assertNotNull(result);
    }

    @Test
    void verifyRegistrationNumberSuccess() throws ClearingHouseException {
        GxLegalRegistrationNumberCredentialSubject cs = new GxLegalRegistrationNumberCredentialSubject();
        cs.setLeiCode("1234");
        cs.setId("valid");
        ExtendedVerifiableCredential result = gxdchService.verifyRegistrationNumber(cs);
        assertNotNull(result);
    }

    @Test
    void verifyRegistrationNumberInvalid() throws ClearingHouseException {
        GxLegalRegistrationNumberCredentialSubject cs = new GxLegalRegistrationNumberCredentialSubject();
        cs.setLeiCode("1234");
        cs.setId("invalid");
        assertThrows(ClearingHouseException.class, () -> gxdchService.verifyRegistrationNumber(cs));
    }


}
