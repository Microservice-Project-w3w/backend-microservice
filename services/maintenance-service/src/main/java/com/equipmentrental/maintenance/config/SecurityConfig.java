package com.equipmentrental.maintenance.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health").permitAll()
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter()
                                )
                        )
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${security.jwt.secret-base64}")
            String secretBase64,

            @Value("${security.jwt.issuer}")
            String issuer
    ) {

        byte[] secretBytes =
                Base64.getDecoder()
                        .decode(secretBase64);

        SecretKeySpec secretKey =
                new SecretKeySpec(
                        secretBytes,
                        "HmacSHA256"
                );

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder
                        .withSecretKey(secretKey)
                        .macAlgorithm(
                                org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256
                        )
                        .build();

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        new JwtTimestampValidator(),
                        new JwtIssuerValidator(issuer)
                )
        );

        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                this::extractAuthorities
        );

        return converter;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {

        Set<String> authorityNames = new LinkedHashSet<>();

        toStringSet(jwt.getClaim("permissions"))
                .forEach(authorityNames::add);

        toStringSet(jwt.getClaim("roles"))
                .forEach(role -> authorityNames.add(
                        role.startsWith("ROLE_")
                                ? role
                                : "ROLE_" + role
                ));

        return authorityNames.stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    private Set<String> toStringSet(Object claim) {

        Set<String> values = new LinkedHashSet<>();

        if (claim instanceof Collection<?> collection) {
            collection.forEach(value -> addString(values, value));
        } else {
            addString(values, claim);
        }

        return values;
    }

    private void addString(Set<String> target, Object value) {

        if (value == null) {
            return;
        }

        String text = String.valueOf(value).trim();

        if (!text.isEmpty()) {
            target.add(text);
        }
    }
}
