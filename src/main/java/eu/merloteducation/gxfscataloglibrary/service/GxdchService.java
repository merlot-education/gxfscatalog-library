package eu.merloteducation.gxfscataloglibrary.service;

import com.danubetech.verifiablecredentials.VerifiablePresentation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;

@Service
public class GxdchService {
    private final ObjectMapper objectMapper;

    private final List<String> complianceServiceUris;

    private final List<String> registryServiceUris;

    private final List<String> notaryServiceUris;

    private final String serviceVersion;

    public GxdchService(@Autowired ObjectMapper objectMapper,
                        @Value("${gxdch-services.compliance-base-uris}") List<String> complianceServiceUris,
                        @Value("${gxdch-services.registry-base-uris}") List<String> registryServiceUris,
                        @Value("${gxdch-services.notary-base-uris}") List<String> notaryServiceUris,
                        @Value("${gxdch-services.version}") String serviceVersion) {
        this.objectMapper = objectMapper;
        this.complianceServiceUris = complianceServiceUris;
        this.registryServiceUris = registryServiceUris;
        this.notaryServiceUris = notaryServiceUris;
        this.serviceVersion = serviceVersion;
    }

    public JsonNode checkCompliance(VerifiablePresentation vp) {
        // go through compliance service uris
        // -> try one uri, then if timeout occurs (an exception is thrown) try next uri

        for (String uri : complianceServiceUris) {
            GxComplianceClient client = getGxComplianceClient(getBaseUriWithVersion(uri));
            // ...
        }

        return null;
    }

    public JsonNode getGxTnCs() {
        // go through registry service uris
        // -> try one uri, then if timeout occurs (an exception is thrown) try next uri
        return null;
    }

    public JsonNode verifyRegistrationNumber(JsonNode registrationNumber){
        // go through notary service uris
        // -> try one uri, then if timeout occurs (an exception is thrown) try next uri
        return null;
    }

    private String getBaseUriWithVersion(String baseUri){
        return baseUri + "/" + serviceVersion;
    }

    private GxComplianceClient getGxComplianceClient(String baseUriWithVersion) {
        HttpServiceProxyFactory httpServiceProxyFactory = getHttpServiceProxyFactory(baseUriWithVersion);
        return httpServiceProxyFactory.createClient(GxComplianceClient.class);
    }

    private GxRegistryClient getGxRegistryClient(String baseUriWithVersion) {
        HttpServiceProxyFactory httpServiceProxyFactory = getHttpServiceProxyFactory(baseUriWithVersion);
        return httpServiceProxyFactory.createClient(GxRegistryClient.class);
    }

    private GxNotaryClient getGxNotaryClient(String baseUriWithVersion) {
        HttpServiceProxyFactory httpServiceProxyFactory = getHttpServiceProxyFactory(baseUriWithVersion);
        return httpServiceProxyFactory.createClient(GxNotaryClient.class);
    }

    private HttpServiceProxyFactory getHttpServiceProxyFactory(String uri) {
        WebClient webClient = WebClient.builder()
                .baseUrl(uri)
                // Set connection and read timeouts
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().responseTimeout(Duration.ofSeconds(30))))
                .build();
        return HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build();
    }
}
