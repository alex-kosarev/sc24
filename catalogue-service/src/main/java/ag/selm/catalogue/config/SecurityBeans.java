package ag.selm.catalogue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityBeans {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/catalogue-api/products")
                        .hasAuthority("SCOPE_edit_catalogue")
                        .requestMatchers(HttpMethod.PATCH, "/catalogue-api/products/{productId:\\d}")
                        .hasAuthority("SCOPE_edit_catalogue")
                        .requestMatchers(HttpMethod.DELETE, "/catalogue-api/products/{productId:\\d}")
                        .hasAuthority("SCOPE_edit_catalogue")
                        .requestMatchers("/actuator/**").hasAuthority("SCOPE_metrics")
                        .requestMatchers(HttpMethod.GET).hasAuthority("SCOPE_view_catalogue")
                        .anyRequest().denyAll())
                .csrf(CsrfConfigurer::disable)
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
                        .jwt(Customizer.withDefaults()))
                .oauth2Client(Customizer.withDefaults())
                .build();
    }
}
