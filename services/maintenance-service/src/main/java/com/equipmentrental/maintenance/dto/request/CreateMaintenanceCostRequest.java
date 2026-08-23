package com.equipmentrental.maintenance.dto.request;

import com.equipmentrental.maintenance.enums.CostType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateMaintenanceCostRequest(

        @NotNull
        CostType costType,

        @NotNull
        @DecimalMin("0.0")
        BigDecimal amount,

        String currencyCode,

        String description
) {
}