package ag.selm.customer.config;

import jakarta.annotation.Priority;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

@Configuration
public class SecurityConfig {

    @Bean
    @Priority(0)
    public SecurityWebFilterChain metricsSecurityWebFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(pathMatchers("/actuator/**"))
                .authorizeExchange(customizer -> customizer.pathMatchers("/actuator/**")
                        .hasAuthority("SCOPE_metrics"))
                .oauth2ResourceServer(customizer -> customizer.jwt(Customizer.withDefaults()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .build();
    }

    @Bean
    @Priority(1)
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(customizer -> customizer.anyExchange().authenticated())
                .oauth2Login(Customizer.withDefaults())
                .oauth2Client(Customizer.withDefaults())
                .build();
    }
}
