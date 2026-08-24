package com.equipmentrental.rental.controller;

import com.equipmentrental.common.security.CurrentUserProvider;
import com.equipmentrental.common.web.BusinessException;
import com.equipmentrental.common.web.CommonErrorCode;
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

        Long customerId = CurrentUserProvider.toLong(
                authentication.getToken().getClaim("customerId")
        );

        if (customerId == null) {
            throw new BusinessException(
                    CommonErrorCode.AUTH_DATA_SCOPE_DENIED,
                    "JWT của CUSTOMER không có customerId hợp lệ"
            );
        }

        return ownershipService.verify(
                orderId,
                equipmentId,
                customerId
        );
    }
}
