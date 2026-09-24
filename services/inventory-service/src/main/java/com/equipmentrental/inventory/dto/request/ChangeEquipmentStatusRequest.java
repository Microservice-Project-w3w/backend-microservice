package com.equipmentrental.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeEquipmentStatusRequest(

        @NotBlank
        String status

) {
}