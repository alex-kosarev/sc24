package ag.selm.manager.config;

import ag.selm.manager.client.RestClientProductsRestClient;
import ag.selm.manager.security.OAuthClientHttpRequestInterceptor;
import de.codecentric.boot.admin.client.registration.BlockingRegistrationClient;
import de.codecentric.boot.admin.client.registration.RegistrationClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ClientBeans {

    @Configuration
    @ConditionalOnProperty(name = "eureka.client.enabled", havingValue = "false")
    public static class StandaloneClientConfig {

        @Bean
        public RestClientProductsRestClient productsRestClient(
                @Value("${selmag.services.catalogue.uri:http://localhost:8081}") String catalogueBaseUri,
                ClientRegistrationRepository clientRegistrationRepository,
                OAuth2AuthorizedClientRepository authorizedClientRepository,
                @Value("${selmag.services.catalogue.registration-id:keycloak}") String registrationId) {
            return new RestClientProductsRestClient(RestClient.builder()
                    .baseUrl(catalogueBaseUri)
                    .requestInterceptor(
                            new OAuthClientHttpRequestInterceptor(
                                    new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository,
                                            authorizedClientRepository), registrationId))
                    .build());
        }
    }


    @Configuration
    @ConditionalOnProperty(name = "eureka.client.enabled", havingValue = "true", matchIfMissing = true)
    public static class CloudClientConfig {

        @Bean
        public RestClientProductsRestClient productsRestClient(
                @Value("${selmag.services.catalogue.uri:http://localhost:8081}") String catalogueBaseUri,
                ClientRegistrationRepository clientRegistrationRepository,
                OAuth2AuthorizedClientRepository authorizedClientRepository,
                @Value("${selmag.services.catalogue.registration-id:keycloak}") String registrationId,
                LoadBalancerClient loadBalancerClient) {
            return new RestClientProductsRestClient(RestClient.builder()
                    .baseUrl(catalogueBaseUri)
                    .requestInterceptor(new LoadBalancerInterceptor(loadBalancerClient))
                    .requestInterceptor(
                            new OAuthClientHttpRequestInterceptor(
                                    new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository,
                                            authorizedClientRepository), registrationId))
                    .build());
        }
    }

    @Bean
    @ConditionalOnProperty(name = "spring.boot.admin.client.enabled", havingValue = "true")
    public RegistrationClient registrationClient(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService
    ) {
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository,
                        authorizedClientService);

        RestTemplate restTemplate = new RestTemplateBuilder()
                .interceptors((request, body, execution) -> {
                    if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(OAuth2AuthorizeRequest
                                .withClientRegistrationId("metrics")
                                .principal("manager-app-metrics-client")
                                .build());

                        request.getHeaders().setBearerAuth(authorizedClient.getAccessToken().getTokenValue());
                    }

                    return execution.execute(request, body);
                })
                .build();
        return new BlockingRegistrationClient(restTemplate);
    }
}
