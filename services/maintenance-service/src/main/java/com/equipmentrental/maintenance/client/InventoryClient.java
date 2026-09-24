package com.equipmentrental.maintenance.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InventoryClient {

    private final RestClient restClient;

    public InventoryClient(
            @Value("${services.inventory.base-url}")
            String baseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public void changeEquipmentStatus(
            Long equipmentId,
            String status
    ) {

        String token = getCurrentAccessToken();

        restClient
                .patch()
                .uri(
                        "/internal/equipment/{id}/status",
                        equipmentId
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .body(
                        new ChangeStatusRequest(status)
                )
                .retrieve()
                .toBodilessEntity();
    }

    private String getCurrentAccessToken() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth
                    .getToken()
                    .getTokenValue();
        }

        throw new IllegalStateException(
                "Không tìm thấy JWT hiện tại"
        );
    }

    public record ChangeStatusRequest(
            String status
    ) {
    }
}