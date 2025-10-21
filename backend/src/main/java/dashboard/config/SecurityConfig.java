package dashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the application.
 * Provides basic authentication and configures which endpoints are accessible.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configure HTTP security
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Allow access to actuator endpoints
                .requestMatchers("/actuator/**").permitAll()
                // Allow access to H2 console
                .requestMatchers("/h2-console/**").permitAll()
                // Allow access to transactions API
                .requestMatchers("/transactions/**").authenticated()
                // Require authentication for all other requests
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> httpBasic.realmName("Security Dashboard")) // Enable HTTP Basic authentication
            .csrf(csrf -> csrf.disable()) // Disable CSRF for API testing
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())); // Allow H2 console frames

        return http.build();
    }

    /**
     * Configure user details service with in-memory users
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin"))
                .roles("ADMIN")
                .build();

        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("password"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }

    /**
     * Password encoder bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
