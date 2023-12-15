package eu.merloteducation.gxfscataloglibrary.config;

import eu.merloteducation.gxfscataloglibrary.service.AuthService;
import eu.merloteducation.gxfscataloglibrary.service.GxfsCatalogClient;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

@Configuration
@EnableScheduling
public class AppConfig {
    @Value("${gxfscatalog.base-uri}")
    private String gxfsCatalogBaseUri;

    @Autowired
    private AuthService authService;

    @Bean
    public GxfsCatalogClient gxfsCatalogClient() {
        WebClient webClient = WebClient.builder()
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                .defaultHeader("Accept", MediaType.APPLICATION_JSON.toString())
                .baseUrl(gxfsCatalogBaseUri)
                .filter(ExchangeFilterFunction.ofRequestProcessor( // add auth header
                        (ClientRequest request) -> Mono.just(
                                ClientRequest.from(request)
                                        .headers(h -> h.setBearerAuth(authService.getAuthToken()))
                                        .build()
                        )
                ))
                .filter(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> { // fix escaped nested json
                    if (clientResponse.statusCode().is2xxSuccessful()) {
                        return clientResponse.bodyToMono(String.class).flatMap(response -> {
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
}
