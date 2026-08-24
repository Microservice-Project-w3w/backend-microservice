package com.equipmentrental.rental.controller;

import com.equipmentrental.rental.dto.response.RentalOwnershipResponse;
import com.equipmentrental.rental.service.RentalOwnershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/rental-orders")
@RequiredArgsConstructor
public class InternalRentalOwnershipController {

    private final RentalOwnershipService ownershipService;

    @GetMapping("/{orderId}/equipment/{equipmentId}/ownership")
    @PreAuthorize("hasRole('CUSTOMER')")
    public RentalOwnershipResponse verifyOwnership(
            @PathVariable Long orderId,
            @PathVariable Long equipmentId,
            JwtAuthenticationToken authentication
    ) {

        Number userIdClaim =
                authentication.getToken()
                        .getClaim("userId");

        if (userIdClaim == null) {
            throw new IllegalStateException(
                    "JWT không có claim userId"
            );
        }

        Long currentUserId =
                userIdClaim.longValue();

        return ownershipService.verify(
                orderId,
                equipmentId,
                currentUserId
        );
    }
}