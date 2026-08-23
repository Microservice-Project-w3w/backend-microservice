package com.equipmentrental.maintenance.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreatePartUsageRequest(

        Long partId,

        String partCodeSnapshot,

        @NotBlank
        String partNameSnapshot,

        @NotNull
        @DecimalMin(value = "0.0001")
        BigDecimal quantity,

        String unit,

        @DecimalMin(value = "0.0")
        BigDecimal unitCost,

        String note
) {
}