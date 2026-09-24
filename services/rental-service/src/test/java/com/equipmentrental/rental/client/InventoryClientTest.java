package com.equipmentrental.rental.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class InventoryClientTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void relaysCurrentBearerTokenToInventory() {
        Jwt jwt = Jwt.withTokenValue("signed-user-token")
                .header("alg", "none")
                .subject("7")
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        HttpHeaders headers = new HttpHeaders();

        InventoryClient.relayBearerToken(headers);

        assertThat(headers.getFirst(HttpHeaders.AUTHORIZATION))
                .isEqualTo("Bearer signed-user-token");
    }
}
