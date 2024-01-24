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
        stubFor(get("/getJSON?name=Test.json")
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"some\": \"data\"}")));
    }

    @Test
    void verifyWizardClientCanCall() {
        gxfsWizardApiClient.getJSON("Test.json");
        verify(getRequestedFor(urlEqualTo("/getJSON?name=Test.json")));
    }

}
