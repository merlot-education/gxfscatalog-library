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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@EnableConfigurationProperties
@WireMockTest(httpPort = 8101)
class GxfsCatalogClientTests {
    @Autowired
    private GxfsCatalogClient gxfsCatalogClient;

    @MockBean
    private GxfsCatalogAuthService gxfsCatalogAuthService;

    @BeforeEach
    public void setUp() {
        lenient().when(gxfsCatalogAuthService.getAuthToken()).thenReturn("1234");

        stubFor(get("/self-descriptions/hash")
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"meta\": null}")));

        stubFor(get(urlMatching("/self-descriptions[^/]*"))
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "totalCount": 1,
                                    "items": [
                                        {
                                            "meta": {
                                                "content": "{\\"@id\\":\\"http://example.edu/verifiablePresentation/self-description1\\"}"
                                            }
                                        }
                                    ]
                                }
                                """)));

        stubFor(delete(urlMatching("/self-descriptions/[^/]*"))
                .willReturn(ok()));

        stubFor(delete(urlMatching("/self-descriptions/error"))
                .willReturn(notFound()));
    }

    @Test
    void clientRequestContainsAuthHeader() {
        gxfsCatalogClient.getSelfDescriptionByHash("hash");
        verify(getRequestedFor(urlEqualTo("/self-descriptions/hash"))
                .withHeader("Authorization", equalTo("Bearer 1234")));
    }

    @Test
    void clientRequestUnescapedJson() {
        GXFSCatalogListResponse<SelfDescriptionItem> items =  gxfsCatalogClient.getSelfDescriptionList(
                null, null, null, null, null,
                null, null, true, true, 0, 0);
        verify(getRequestedFor(urlMatching("/self-descriptions[^/]*")));
        assertNotNull(items.getItems().get(0).getMeta().getContent().getId());
    }

    @Test
    void clientRequestEmptyBody() {
        gxfsCatalogClient.deleteSelfDescriptionByHash("hash");
        verify(deleteRequestedFor(urlMatching("/self-descriptions/[^/]*")));
    }

    @Test
    void clientRequestError() {
        assertThrows(WebClientResponseException.NotFound.class,
                () -> gxfsCatalogClient.deleteSelfDescriptionByHash("error"));
        verify(deleteRequestedFor(urlMatching("/self-descriptions/error")));
    }

}
