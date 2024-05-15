package eu.merloteducation.gxfscataloglibrary.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@EnableConfigurationProperties
class GxdchServiceTests {
    @Autowired
    private GxdchService gxdchService;

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
    @Disabled
    void checkComplianceSuccess() {
        JsonNode result = gxdchService.checkCompliance(null);
        assertNotNull(result);
    }

    @Test
    @Disabled
    void getTncSuccess() {
        JsonNode result = gxdchService.getGxTnCs();
        assertNotNull(result);
    }

    @Test
    @Disabled
    void verifyRegistrationNumberSuccess() {
        JsonNode result = gxdchService.verifyRegistrationNumber(null);
        assertNotNull(result);
    }


}
