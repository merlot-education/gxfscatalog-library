package eu.merloteducation.gxfscataloglibrary.config;

import eu.merloteducation.gxfscataloglibrary.service.AuthService;
import eu.merloteducation.gxfscataloglibrary.service.GxfsCatalogClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

@Configuration
public class AppConfig {
    @Value("${gxfscatalog.base-uri}")
    private String gxfsCatalogBaseUri;

    @Autowired
    private AuthService authService;

    @Bean
    public GxfsCatalogClient gxfsCatalogClient() {
        WebClient webClient = WebClient.builder()
                .baseUrl(gxfsCatalogBaseUri)
                .filter(ExchangeFilterFunction.ofRequestProcessor(
                        (ClientRequest request) -> Mono.just(
                                ClientRequest.from(request)
                                        .header("Authorization", authService.getToken())
                                        .build()
                        )
                ))
                .build();
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build();
        return httpServiceProxyFactory.createClient(GxfsCatalogClient.class);
    }
}
