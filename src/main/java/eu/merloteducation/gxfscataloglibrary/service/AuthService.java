package eu.merloteducation.gxfscataloglibrary.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class AuthService {

    private final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final WebClient webClient;
    private final String keycloakTokenUri;
    private final String keycloakLogoutUri;
    private final String clientId;
    private final String clientSecret;
    private final String grantType;
    private final String keycloakGXFScatalogUser;
    private final String keycloakGXFScatalogPass;

    @Getter
    private String authToken;
    private String refreshToken;

    public AuthService(@Value("${keycloak.token-uri}") String keycloakTokenUri,
                       @Value("${keycloak.logout-uri}") String keycloakLogoutUri,
                       @Value("${keycloak.client-id}") String clientId,
                       @Value("${keycloak.client-secret}") String clientSecret,
                       @Value("${keycloak.authorization-grant-type}") String grantType,
                       @Value("${keycloak.gxfscatalog-user}") String keycloakGXFScatalogUser,
                       @Value("${keycloak.gxfscatalog-pass}") String keycloakGXFScatalogPass,
                       @Autowired WebClient.Builder webClientBuilder) {
        this.keycloakTokenUri = keycloakTokenUri;
        this.keycloakLogoutUri = keycloakLogoutUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.grantType = grantType;
        this.keycloakGXFScatalogUser = keycloakGXFScatalogUser;
        this.keycloakGXFScatalogPass = keycloakGXFScatalogPass;
        this.webClient = webClientBuilder.build();
        this.refreshLogin();
    }

    @Scheduled(fixedDelay = 120 * 1000)
    public void refreshLogin() {
        // TODO compute delay dynamically from token
        try {
            loginAsGXFSCatalog();
        } catch (WebClientRequestException | WebClientResponseException e) {
            logger.warn("Failed to refresh authentication token", e);
        }
    }

    private void loginAsGXFSCatalog() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", keycloakGXFScatalogUser);
        map.add("password", keycloakGXFScatalogPass);
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("grant_type", grantType);

        JsonNode loginResult = webClient.post()
                .uri(keycloakTokenUri)
                .body(BodyInserters.fromValue(map))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        if (loginResult != null && loginResult.has("access_token")) {
            String previousRefreshToken = this.refreshToken;
            // store new tokens
            this.authToken = loginResult.get("access_token").asText();
            this.refreshToken = loginResult.get("refresh_token").asText();
            // end previous session
            logoutAsGXFSCatalog(previousRefreshToken);
        }
    }

    private void logoutAsGXFSCatalog(String refreshToken) {
        if (refreshToken == null) {
            return;
        }

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("refresh_token", refreshToken);

        webClient.post()
                .uri(keycloakLogoutUri)
                .body(BodyInserters.fromValue(map))
                .retrieve().toBodilessEntity().subscribe();
    }
}
