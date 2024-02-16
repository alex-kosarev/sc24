package ag.selm.catalogue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.mockito.Mockito.mock;

@Configuration
public class TestingBeans {

    @Bean
    public JwtDecoder jwtDecoder() {
        return mock();
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return mock();
    }
}
