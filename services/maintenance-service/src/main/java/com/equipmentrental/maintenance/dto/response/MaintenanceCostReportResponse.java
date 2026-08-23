package com.equipmentrental.maintenance.dto.response;

import java.math.BigDecimal;

public record MaintenanceCostReportResponse(

        BigDecimal partCost,

        BigDecimal laborCost,

        BigDecimal outsourceCost,

        BigDecimal transportCost,

        BigDecimal otherCost,

        BigDecimal totalCost
) {
}