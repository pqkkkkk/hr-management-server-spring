package org.pqkkkkk.hr_management_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Development security configuration.
 * Active only when profile 'dev' is set. Permits access to API and H2 console without authentication.
 * NOTE: For local/dev only. Do NOT use in production.
 */
@Configuration
@Profile("dev")
public class DevSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Tắt CSRF để H2 console chạy được
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**", "/api/**", "/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable())); // Cho phép iframe H2 console

        return http.build();
    }
}
