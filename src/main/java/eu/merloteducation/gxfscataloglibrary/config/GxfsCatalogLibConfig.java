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
import org.springframework.web.reactive.function.client.ExchangeStrategies;
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

    private static final int EXCHANGE_STRATEGY_SIZE = 16 * 1024 * 1024;
    private static final ExchangeStrategies EXCHANGE_STRATEGIES = ExchangeStrategies.builder()
            .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(EXCHANGE_STRATEGY_SIZE))
            .build();

    @Bean
    public GxfsCatalogClient gxfsCatalogClient(@Autowired GxfsCatalogAuthService gxfsCatalogAuthService) {
        WebClient webClient = WebClient.builder()
                .exchangeStrategies(EXCHANGE_STRATEGIES)
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
                .exchangeStrategies(EXCHANGE_STRATEGIES)
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
        webClientBuilder.exchangeStrategies(EXCHANGE_STRATEGIES);
        return webClientBuilder.build();
    }

    @Bean
    public Map<String, GxComplianceClient> gxComplianceClients() {
        Map<String, GxComplianceClient> clients = new HashMap<>();
        for (String clientUri : complianceServiceUris) {
            HttpServiceProxyFactory httpServiceProxyFactory = getHttpServiceProxyFactory(clientUri);
            clients.put(clientUri, httpServiceProxyFactory.createClient(GxComplianceClient.class));
        }
        return clients;
    }

    @Bean
    public Map<String, GxRegistryClient> gxRegistryClients() {
        Map<String, GxRegistryClient> clients = new HashMap<>();
        for (String clientUri : registryServiceUris) {
            HttpServiceProxyFactory httpServiceProxyFactory = getHttpServiceProxyFactory(clientUri);
            clients.put(clientUri, httpServiceProxyFactory.createClient(GxRegistryClient.class));
        }
        return clients;
    }

    @Bean
    public Map<String, GxNotaryClient> gxNotaryClients() {
        Map<String, GxNotaryClient> clients = new HashMap<>();
        for (String clientUri : notaryServiceUris) {
            HttpServiceProxyFactory httpServiceProxyFactory = getHttpServiceProxyFactory(clientUri);
            clients.put(clientUri, httpServiceProxyFactory.createClient(GxNotaryClient.class));
        }
        return clients;
    }

    private HttpServiceProxyFactory getHttpServiceProxyFactory(String uri) {
        WebClient webClient = WebClient.builder()
                .exchangeStrategies(EXCHANGE_STRATEGIES)
                .baseUrl(uri)
                // Set connection and read timeouts
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().responseTimeout(Duration.ofSeconds(30))))
                .build();
        return HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build();
    }

}
