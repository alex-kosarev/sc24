package ag.selm.customer.config;

import ag.selm.customer.client.WebClientProductsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfig {

    @Bean
    public WebClientProductsClient webClientProductsClient(
            @Value("${selmag.services.catalogue.uri:http://localhost:8081}") String catalogueBaseUrl
    ) {
        return new WebClientProductsClient(WebClient.builder()
                .baseUrl(catalogueBaseUrl)
                .build());
    }
}
