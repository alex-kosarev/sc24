package ag.selm.config.config;

import jakarta.annotation.Priority;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

@Configuration
public class SecurityBeans {

    private static final String NOOP_PASSWORD_PREFIX = "{noop}";

    private static final Pattern PASSWORD_ALGORITHM_PATTERN = Pattern.compile("^\\{.+}.*$");

    private static final Log logger = LogFactory.getLog(SecurityBeans.class);

    @Bean
    @Priority(0)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/actuator/**")
                .authorizeHttpRequests(configurer -> configurer
                        .requestMatchers("/actuator/**").hasAuthority("SCOPE_metrics"))
                .oauth2ResourceServer(configurer -> configurer.jwt(Customizer.withDefaults()))
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(CsrfConfigurer::disable)
                .build();
    }

    @Bean
    @Priority(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   SecurityProperties properties,
                                                   ObjectProvider<PasswordEncoder> passwordEncoder) throws Exception {
        SecurityProperties.User user = properties.getUser();
        List<String> roles = user.getRoles();
//        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
//        authenticationProvider.setUserDetailsService(userDetailsService);
        return http
                .authorizeHttpRequests(customizer -> customizer.anyRequest().authenticated())
                .csrf(CsrfConfigurer::disable)
                .sessionManagement(customizer ->
                        customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .userDetailsService(new InMemoryUserDetailsManager(User.withUsername(user.getName())
                        .password(getOrDeducePassword(user, passwordEncoder.getIfAvailable()))
                        .roles(StringUtils.toStringArray(roles))
                        .build()))
                .build();
    }

    private String getOrDeducePassword(SecurityProperties.User user, PasswordEncoder encoder) {
        String password = user.getPassword();
        if (user.isPasswordGenerated()) {
            logger.warn(String.format(
                    "%n%nUsing generated security password: %s%n%nThis generated password is for development use only. "
                            + "Your security configuration must be updated before running your application in "
                            + "production.%n",
                    user.getPassword()));
        }
        if (encoder != null || PASSWORD_ALGORITHM_PATTERN.matcher(password).matches()) {
            return password;
        }
        return NOOP_PASSWORD_PREFIX + password;
    }
}
