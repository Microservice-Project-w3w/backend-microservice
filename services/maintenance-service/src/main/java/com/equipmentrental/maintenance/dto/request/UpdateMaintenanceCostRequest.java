package com.equipmentrental.maintenance.dto.request;

import com.equipmentrental.maintenance.enums.CostType;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record UpdateMaintenanceCostRequest(

        CostType costType,

        @DecimalMin("0.0")
        BigDecimal amount,

        String currencyCode,

        String description
) {
}