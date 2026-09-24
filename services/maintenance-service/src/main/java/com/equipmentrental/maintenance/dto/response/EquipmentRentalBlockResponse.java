package com.equipmentrental.maintenance.dto.response;

public record EquipmentRentalBlockResponse(

        Long equipmentId,

        boolean blocked,

        String reason

) {
}