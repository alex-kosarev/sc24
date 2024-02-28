package ag.selm.eureka.config;

import jakarta.annotation.Priority;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityBeans {

    @Bean
    @Priority(0)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/eureka/apps", "/eureka/apps/**", "/actuator/**")
                .authorizeHttpRequests(customizer -> customizer
                        .requestMatchers("/actuator/**").hasAuthority("SCOPE_metrics")
                        .requestMatchers("/eureka/apps", "/eureka/apps/**").hasAuthority("SCOPE_discovery")
                        .anyRequest().denyAll())
                .sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(CsrfConfigurer::disable)
                .oauth2ResourceServer(customizer -> customizer.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    @Priority(1)
    public SecurityFilterChain mainSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(customizer -> customizer.anyRequest().authenticated())
                .oauth2Login(Customizer.withDefaults())
                .build();
    }
}
