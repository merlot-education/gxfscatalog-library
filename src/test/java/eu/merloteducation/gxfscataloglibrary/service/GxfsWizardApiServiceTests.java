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
public class GxfsWizardApiServiceTests {

    @Autowired
    private GxfsWizardApiService gxfsWizardApiService;
    @MockBean
    private GxfsWizardApiClient gxfsWizardApiClient;
    @MockBean
    private GxfsCatalogService gxfsCatalogService;
    @MockBean
    private GxfsCatalogAuthService gxfsCatalogAuthService;
    @MockBean
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
        String json = gxfsWizardApiService.getShapeByName("Participant1.json");
        assertNotNull(json);
    }

    @Test
    void getShapeFileNonExistent() {
        WebClientResponseException e = assertThrows(WebClientResponseException.class,
                () -> gxfsWizardApiService.getShapeByName("missing"));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }
}
