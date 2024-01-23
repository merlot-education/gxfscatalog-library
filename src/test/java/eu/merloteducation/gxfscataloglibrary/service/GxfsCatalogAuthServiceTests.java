package eu.merloteducation.gxfscataloglibrary.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class GxfsCatalogAuthServiceTests {
    @Mock
    private WebClient webClient;

    @Mock
    WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    WebClient.RequestHeadersSpec loginRequestHeadersSpec;

    @Mock
    WebClient.RequestBodySpec loginRequestBodySpec;

    @Mock
    WebClient.ResponseSpec loginResponseSpec;

    @Mock
    WebClient.RequestHeadersSpec logoutRequestHeadersSpec;

    @Mock
    WebClient.RequestBodySpec logoutRequestBodySpec;

    @Mock
    WebClient.ResponseSpec logoutResponseSpec;

    private GxfsCatalogAuthService gxfsCatalogAuthService;


    @BeforeEach
    public void setUp() throws JsonProcessingException {
        String keycloakTokenUri = "http://example.com/token";
        String keycloakLogoutUri = "http://example.com/logout";
        String loginResponse = """
                {
                    "access_token": "1234",
                    "refresh_token": "5678"
                }
                """;
        ObjectMapper mapper = new ObjectMapper();

        lenient().when(webClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.uri(eq(keycloakTokenUri))).thenReturn(loginRequestBodySpec);
        lenient().when(loginRequestBodySpec.body(any())).thenReturn(loginRequestHeadersSpec);
        lenient().when(loginRequestHeadersSpec.retrieve()).thenReturn(loginResponseSpec);
        lenient().when(loginResponseSpec.bodyToMono(eq(JsonNode.class)))
                .thenReturn(Mono.just(mapper.readTree(loginResponse)));

        lenient().when(requestBodyUriSpec.uri(eq(keycloakLogoutUri))).thenReturn(logoutRequestBodySpec);
        lenient().when(logoutRequestBodySpec.body(any())).thenReturn(logoutRequestHeadersSpec);
        lenient().when(logoutRequestHeadersSpec.retrieve()).thenReturn(logoutResponseSpec);
        lenient().when(logoutResponseSpec.toBodilessEntity())
                .thenReturn(Mono.empty());

        gxfsCatalogAuthService = new GxfsCatalogAuthService(
                keycloakTokenUri,
                keycloakLogoutUri,
                "client",
                "secret",
                "password",
                "user",
                "pass",
                webClient);
    }

    @Test
    void testRefresh(){
        gxfsCatalogAuthService.refreshLogin();
        assertNotNull(gxfsCatalogAuthService.getAuthToken());
        assertEquals("1234", gxfsCatalogAuthService.getAuthToken());
    }
}
