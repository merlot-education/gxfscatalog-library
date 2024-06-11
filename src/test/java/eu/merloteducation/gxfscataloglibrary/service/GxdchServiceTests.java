package eu.merloteducation.gxfscataloglibrary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiableCredential;
import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiablePresentation;
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
    void checkComplianceSuccess() {
        ExtendedVerifiablePresentation vp = new ExtendedVerifiablePresentation();
        vp.setJsonObjectKeyValue("id", "valid");
        ExtendedVerifiableCredential result = gxdchService.checkCompliance("valid", vp);
        assertNotNull(result);
    }

    @ParameterizedTest
    @CsvSource({
            "badsignature",
            "badcert",
            "badshape"
    })
    void checkComplianceBad(String shapeName) {
        ExtendedVerifiablePresentation vp = new ExtendedVerifiablePresentation();
        vp.setJsonObjectKeyValue("id", shapeName);
        ExtendedVerifiableCredential result = gxdchService.checkCompliance(shapeName, vp);
        assertNull(result);
    }

    @Test
    void getTncSuccess() {
        JsonNode result = gxdchService.getGxTnCs();
        assertNotNull(result);
    }

    @Test
    void verifyRegistrationNumberSuccess() {
        GxLegalRegistrationNumberCredentialSubject cs = new GxLegalRegistrationNumberCredentialSubject();
        cs.setLeiCode("1234");
        cs.setId("valid");
        ExtendedVerifiableCredential result = gxdchService.verifyRegistrationNumber("someissuer", cs);
        assertNotNull(result);
    }

    @Test
    void verifyRegistrationNumberInvalid() {
        GxLegalRegistrationNumberCredentialSubject cs = new GxLegalRegistrationNumberCredentialSubject();
        cs.setLeiCode("1234");
        cs.setId("invalid");
        ExtendedVerifiableCredential result = gxdchService.verifyRegistrationNumber("someissuer", cs);
        assertNull(result);
    }


}
