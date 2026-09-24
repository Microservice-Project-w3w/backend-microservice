package com.equipmentrental.rental.dto.response;

public record RentalOwnershipResponse(
        boolean owned,
        Long rentalOrderId,
        Long customerId,
        Long organizationId,
        Long branchId,
        Long equipmentId
) {
}