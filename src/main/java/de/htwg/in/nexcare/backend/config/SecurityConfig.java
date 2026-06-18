package de.htwg.in.nexcare.backend.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration.
 *
 * Schutzziele:
 *   Authentizität   – JWT via Auth0 (OAuth2 Resource Server)
 *   Vertraulichkeit – Patientendaten nur für authentifizierte Nutzer; Rollen-Checks in Controllern
 *   Integrität      – Bean Validation (@Valid) auf allen Entitäten; HTTPS via HSTS
 *   Verfügbarkeit   – keine destruktiven öffentlichen Endpunkte
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(withDefaults())
                .headers(headers -> headers
                    .contentTypeOptions(withDefaults())
                    .frameOptions(frame -> frame.deny())
                    .httpStrictTransportSecurity(hsts -> hsts
                        .includeSubDomains(true)
                        .maxAgeInSeconds(31536000))
                )
                .authorizeHttpRequests(authorize -> authorize

                    // ── Public (unauthenticated) ─────────────────────────────
                    // Klinikumnamen sind nicht personenbezogen – öffentlich lesbar
                    .requestMatchers(HttpMethod.GET, "/api/klinikum", "/api/klinikum/*").permitAll()

                    // ── Alle anderen /api/** Endpunkte: Login erforderlich ───
                    // Rollen-Checks erfolgen in den Controllern via SecurityService
                    .requestMatchers("/api/**").authenticated()

                    // ── Nicht-API Pfade ──────────────────────────────────────
                    .anyRequest().permitAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
                .build();
    }
}
