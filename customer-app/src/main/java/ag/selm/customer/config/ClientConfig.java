package ag.selm.customer.config;

import ag.selm.customer.client.WebClientFavouriteProductsClient;
import ag.selm.customer.client.WebClientProductReviewsClient;
import ag.selm.customer.client.WebClientProductsClient;
import de.codecentric.boot.admin.client.config.ClientProperties;
import de.codecentric.boot.admin.client.registration.ReactiveRegistrationClient;
import de.codecentric.boot.admin.client.registration.RegistrationClient;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.DefaultClientRequestObservationConvention;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfig {

    @Configuration
    @ConditionalOnProperty(name = "eureka.client.enabled", havingValue = "false")
    public static class StandaloneClientConfig {

        @Bean
        @Scope("prototype")
        public WebClient.Builder selmagServicesWebClientBuilder(
                ReactiveClientRegistrationRepository clientRegistrationRepository,
                ServerOAuth2AuthorizedClientRepository authorizedClientRepository,
                ObservationRegistry observationRegistry
        ) {
            ServerOAuth2AuthorizedClientExchangeFilterFunction filter =
                    new ServerOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrationRepository,
                            authorizedClientRepository);
            filter.setDefaultClientRegistrationId("keycloak");
            return WebClient.builder()
                    .observationRegistry(observationRegistry)
                    .observationConvention(new DefaultClientRequestObservationConvention())
                    .filter(filter);
        }
    }

    @Configuration
    @ConditionalOnProperty(name = "eureka.client.enabled", havingValue = "true", matchIfMissing = true)
    public static class CloudClientConfig {

        @Bean
        @LoadBalanced
        @Scope("prototype")
        public WebClient.Builder selmagServicesWebClientBuilder(
                ReactiveClientRegistrationRepository clientRegistrationRepository,
                ServerOAuth2AuthorizedClientRepository authorizedClientRepository,
                ObservationRegistry observationRegistry
        ) {
            ServerOAuth2AuthorizedClientExchangeFilterFunction filter =
                    new ServerOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrationRepository,
                            authorizedClientRepository);
            filter.setDefaultClientRegistrationId("keycloak");
            return WebClient.builder()
                    .observationRegistry(observationRegistry)
                    .observationConvention(new DefaultClientRequestObservationConvention())
                    .filter(filter);
        }
    }

    @Bean
    public WebClientProductsClient webClientProductsClient(
            @Value("${selmag.services.catalogue.uri:http://localhost:8081}") String catalogueBaseUrl,
            WebClient.Builder selmagServicesWebClientBuilder
    ) {
        return new WebClientProductsClient(selmagServicesWebClientBuilder
                .baseUrl(catalogueBaseUrl)
                .build());
    }

    @Bean
    public WebClientFavouriteProductsClient webClientFavouriteProductsClient(
            @Value("${selmag.services.feedback.uri:http://localhost:8084}") String feedbackBaseUrl,
            WebClient.Builder selmagServicesWebClientBuilder
    ) {
        return new WebClientFavouriteProductsClient(selmagServicesWebClientBuilder
                .baseUrl(feedbackBaseUrl)
                .build());
    }

    @Bean
    public WebClientProductReviewsClient webClientProductReviewsClient(
            @Value("${selmag.services.feedback.uri:http://localhost:8084}") String feedbackBaseUrl,
            WebClient.Builder selmagServicesWebClientBuilder
    ) {
        return new WebClientProductReviewsClient(selmagServicesWebClientBuilder
                .baseUrl(feedbackBaseUrl)
                .build());
    }

    @Bean
    @ConditionalOnProperty(name = "spring.boot.admin.client.enabled", havingValue = "true")
    public RegistrationClient registrationClient(
            ClientProperties clientProperties,
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ReactiveOAuth2AuthorizedClientService authorizedClientService
    ) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction filter =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                        new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrationRepository,
                                authorizedClientService));
        filter.setDefaultClientRegistrationId("metrics");

        return new ReactiveRegistrationClient(WebClient.builder()
                .filter(filter)
                .build(), clientProperties.getReadTimeout());
    }
}
