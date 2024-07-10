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

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.GXFSCatalogListResponse;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.SelfDescriptionItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@EnableConfigurationProperties
@WireMockTest(httpPort = 8102)
class GxfsWizardApiClientTests {
    @Autowired
    private GxfsWizardApiClient gxfsWizardApiClient;

    @BeforeEach
    public void setUp() {
        stubFor(get("/getJSON?ecosystem=ecosystem&name=Test.json")
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"some\": \"data\"}")));
    }

    @Test
    void verifyWizardClientCanCall() {
        gxfsWizardApiClient.getJSON("ecosystem", "Test.json");
        verify(getRequestedFor(urlEqualTo("/getJSON?ecosystem=ecosystem&name=Test.json")));
    }

}
