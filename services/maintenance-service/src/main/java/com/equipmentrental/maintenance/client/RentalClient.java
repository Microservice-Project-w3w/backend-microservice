package com.equipmentrental.maintenance.client;

import com.equipmentrental.maintenance.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RentalClient {

    private final RestClient restClient;

    public RentalClient(
            @Value("${services.rental.base-url}")
            String baseUrl
    ) {
        this.restClient =
                RestClient.builder()
                        .baseUrl(baseUrl)
                        .build();
    }

    public RentalOwnershipResponse
    verifyOwnership(
            Long rentalOrderId,
            Long equipmentId
    ) {

        String token =
                getCurrentAccessToken();

        try {

            RentalOwnershipResponse response =
                    restClient.get()
                            .uri(
                                    "/internal/rental-orders/{orderId}/equipment/{equipmentId}/ownership",
                                    rentalOrderId,
                                    equipmentId
                            )
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    "Bearer " + token
                            )
                            .retrieve()
                            .body(
                                    RentalOwnershipResponse.class
                            );

            if (
                    response == null
                            ||
                            !response.owned()
            ) {
                throw new BusinessException(
                        "Bạn không có quyền báo sự cố cho thiết bị này"
                );
            }

            return response;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(
                    "Không thể xác minh đơn thuê với Rental Service"
            );
        }
    }

    private String getCurrentAccessToken() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication
                        instanceof JwtAuthenticationToken jwt
        ) {
            return jwt.getToken()
                    .getTokenValue();
        }

        throw new IllegalStateException(
                "Không tìm thấy JWT hiện tại"
        );
    }

    public record RentalOwnershipResponse(

            boolean owned,

            Long rentalOrderId,

            Long customerId,

            Long organizationId,

            Long branchId,

            Long equipmentId
    ) {
    }
}