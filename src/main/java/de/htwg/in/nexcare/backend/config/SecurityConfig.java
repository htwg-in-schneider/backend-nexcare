package de.htwg.in.nexcare.backend.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/** Configures Spring Security: public read access, authenticated writes, admin-only admin area. */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        // public read
                        .requestMatchers(HttpMethod.GET, "/api/patient", "/api/patient/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/klinikum", "/api/klinikum/*").permitAll()
                        // user profile
                        .requestMatchers("/api/profile").authenticated()
                        // patient writes require login
                        .requestMatchers(HttpMethod.POST, "/api/patient").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/patient/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/patient/*").authenticated()
                        // medikament catalog: public reads, admin writes
                        .requestMatchers(HttpMethod.GET, "/api/medikament", "/api/medikament/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/medikament").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/medikament/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/medikament/*").authenticated()
                        // medikamentenplan: authenticated
                        .requestMatchers("/api/patient/*/medikamentenplan/**").authenticated()
                        // betten struktur + zuweisung: authenticated
                        .requestMatchers(HttpMethod.GET, "/api/betten/struktur").authenticated()
                        .requestMatchers("/api/betten/**").authenticated()
                        // admin area
                        .requestMatchers("/api/admin/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/klinikum").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/klinikum/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/klinikum/*").authenticated()
                        .anyRequest().permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
                .build();
    }
}
