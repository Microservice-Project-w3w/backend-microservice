package com.equipmentrental.maintenance.dto.request;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record UpdatePartUsageRequest(

        @DecimalMin(value = "0.0001")
        BigDecimal quantity,

        String unit,

        @DecimalMin(value = "0.0")
        BigDecimal unitCost,

        String note
) {
}