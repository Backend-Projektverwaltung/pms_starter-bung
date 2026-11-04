package de.szut.pms.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.oauth2.jwt.JwtValidators;

import java.util.List;

/**
 * Verdrahtung fertig, Endpunkte nicht: Trage hier deine eigenen Pfade in
 * authorizeHttpRequests() ein, sobald du sie implementiert hast. Alles
 * andere — JWT-Prüfung gegen Authentik, CORS — steht bereits.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
@ConditionalOnProperty(value = "authentik.enabled", matchIfMissing = true)
public class AuthentikSecurityConfig {

    @Value("${authentik.jwk-set-uri}")
    private String jwkSetUri;
    @Value("${authentik.issuer-uri}")
    private String issuerUri;

    @Bean
    public JwtDecoder jwtDecoder() {

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(issuerUri)
        );

        return decoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {

                        })
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/projects", "/projects/**").authenticated()
                        .requestMatchers("/employees/**").authenticated()
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("*"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}