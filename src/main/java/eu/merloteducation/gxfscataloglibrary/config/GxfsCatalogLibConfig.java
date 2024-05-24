package eu.merloteducation.gxfscataloglibrary.config;

import eu.merloteducation.gxfscataloglibrary.service.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@PropertySource("classpath:application.yml")
@EnableScheduling
public class GxfsCatalogLibConfig {
    @Value("${gxfscatalog.base-uri:#{null}}")
    private String gxfsCatalogBaseUri;

    @Value("${gxfswizardapi.base-uri:#{null}}")
    private String gxfsWizardApiBaseUri;

    @Value("${gxfscatalog-library.ignore-ssl:false}")
    private boolean ignoreSsl;

    @Value("${gxdch-services.compliance-base-uris:}")
    private List<String> complianceServiceUris;
    @Value("${gxdch-services.registry-base-uris:}")
    private List<String> registryServiceUris;
    @Value("${gxdch-services.notary-base-uris:}")
    private List<String> notaryServiceUris;
    @Value("${gxdch-services.version:#{null}}")
    private String serviceVersion;

    @Bean
    public GxfsCatalogClient gxfsCatalogClient(@Autowired GxfsCatalogAuthService gxfsCatalogAuthService) {
        WebClient webClient = WebClient.builder()
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                .defaultHeader("Accept", MediaType.APPLICATION_JSON.toString())
                .baseUrl(gxfsCatalogBaseUri)
                .filter(ExchangeFilterFunction.ofRequestProcessor( // add auth header
                        (ClientRequest request) -> Mono.just(
                                ClientRequest.from(request)
                                        .headers(h -> h.setBearerAuth(gxfsCatalogAuthService.getAuthToken()))
                                        .build()
                        )
                ))
                .filter(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> { // fix escaped nested json
                    if (clientResponse.statusCode().is2xxSuccessful()) {
                        return clientResponse.bodyToMono(String.class)
                                .switchIfEmpty(Mono.just(""))
                                .flatMap(response -> {
                            response = StringEscapeUtils.unescapeJson(response)
                                    .replace("\"{", "{")
                                    .replace("}\"", "}");
                            return Mono.just(clientResponse.mutate().body(response).build());
                        });
                    } else {
                        return Mono.just(clientResponse);
                    }
                }))
                .build();
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build();
        return httpServiceProxyFactory.createClient(GxfsCatalogClient.class);
    }

    @Bean
    public GxfsWizardApiClient gxfsWizardApiClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(gxfsWizardApiBaseUri)
                .build();
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build();
        return httpServiceProxyFactory.createClient(GxfsWizardApiClient.class);
    }

    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) throws SSLException {
        if (ignoreSsl) {
            SslContext context = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();

            HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(context));

            webClientBuilder.clientConnector(new ReactorClientHttpConnector(httpClient));
        }
        return webClientBuilder.build();
    }

    @Bean
    public Map<String, GxComplianceClient> gxComplianceClients() {
        Map<String, GxComplianceClient> clients = new HashMap<>();
        for (String clientUri : complianceServiceUris) {
            String fullUri = getBaseUriWithVersion(clientUri);
            HttpServiceProxyFactory httpServiceProxyFactory = getHttpServiceProxyFactory(fullUri);
            clients.put(fullUri, httpServiceProxyFactory.createClient(GxComplianceClient.class));
        }
        return clients;
    }

    @Bean
    public Map<String, GxRegistryClient> gxRegistryClients() {
        Map<String, GxRegistryClient> clients = new HashMap<>();
        for (String clientUri : registryServiceUris) {
            String fullUri = getBaseUriWithVersion(clientUri);
            HttpServiceProxyFactory httpServiceProxyFactory = getHttpServiceProxyFactory(fullUri);
            clients.put(fullUri, httpServiceProxyFactory.createClient(GxRegistryClient.class));
        }
        return clients;
    }

    @Bean
    public Map<String, GxNotaryClient> gxNotaryClients() {
        Map<String, GxNotaryClient> clients = new HashMap<>();
        for (String clientUri : notaryServiceUris) {
            String fullUri = clientUri + "/" + "development"; //getBaseUriWithVersion(clientUri); // TODO remove
            HttpServiceProxyFactory httpServiceProxyFactory = getHttpServiceProxyFactory(fullUri);
            clients.put(fullUri, httpServiceProxyFactory.createClient(GxNotaryClient.class));
        }
        return clients;
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

    private String getBaseUriWithVersion(String baseUri){
        return baseUri + "/" + serviceVersion;
    }

}
