package com.equipmentrental.inventory.dto.response;

public record ReservationEquipmentOwnershipResponse(
        boolean exists,
        Long reservationId,
        Long rentalOrderId,
        Long organizationId,
        Long branchId,
        Long equipmentId
) {
}