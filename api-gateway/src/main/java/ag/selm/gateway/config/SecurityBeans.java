package ag.selm.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;

@Configuration
public class SecurityBeans {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/actuator/**"))
                .authorizeExchange(configurer -> configurer
                        .pathMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .pathMatchers("/actuator/**").hasAuthority("SCOPE_metrics"))
                .oauth2ResourceServer(configurer -> configurer.jwt(Customizer.withDefaults()))
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }
}
