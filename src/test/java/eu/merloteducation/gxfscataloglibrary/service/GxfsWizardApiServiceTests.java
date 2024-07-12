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

import eu.merloteducation.gxfscataloglibrary.config.GxfsCatalogLibConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@EnableConfigurationProperties
class GxfsWizardApiServiceTests {

    @Autowired
    private GxfsWizardApiService gxfsWizardApiService;
    @MockBean
    private GxfsWizardApiClient gxfsWizardApiClient;
    @MockBean
    private GxfsCatalogService gxfsCatalogService;
    @MockBean
    private GxfsCatalogAuthService gxfsCatalogAuthService;
    @Autowired
    private GxfsCatalogLibConfig gxfsCatalogLibConfig;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(gxfsWizardApiService, "gxfsWizardApiClient", new GxfsWizardApiClientFake());
    }

    @Test
    void getShapesByExistingEcosystem() {
        Map<String, List<String>> shapes = gxfsWizardApiService.getShapesByEcosystem("ecosystem1");
        assertNotNull(shapes);
    }

    @Test
    void getShapesByNonExistentEcosystem() {
        WebClientResponseException e = assertThrows(WebClientResponseException.class,
                () -> gxfsWizardApiService.getShapesByEcosystem("missing"));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }

    @Test
    void getOfferingShapesByExistingEcosystem() {
        List<String> shapes = gxfsWizardApiService.getServiceOfferingShapesByEcosystem("ecosystem1");
        assertNotNull(shapes);
        assertEquals(2, shapes.size());
    }

    @Test
    void getOfferingShapesByNonExistentEcosystem() {
        WebClientResponseException e = assertThrows(WebClientResponseException.class,
                () -> gxfsWizardApiService.getServiceOfferingShapesByEcosystem("missing"));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }

    @Test
    void getParticipantShapesByExistingEcosystem() {
        List<String> shapes = gxfsWizardApiService.getParticipantShapesByEcosystem("ecosystem1");
        assertNotNull(shapes);
        assertEquals(1, shapes.size());
    }

    @Test
    void getParticipantShapesByNonExistentEcosystem() {
        WebClientResponseException e = assertThrows(WebClientResponseException.class,
                () -> gxfsWizardApiService.getParticipantShapesByEcosystem("missing"));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }

    @Test
    void getShapeFileExisting() {
        String json = gxfsWizardApiService.getShapeByName("ecosystem","Participant1.json");
        assertNotNull(json);
    }

    @Test
    void getShapeFileNonExistent() {
        WebClientResponseException e = assertThrows(WebClientResponseException.class,
                () -> gxfsWizardApiService.getShapeByName("ecosystem","missing"));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }
}
