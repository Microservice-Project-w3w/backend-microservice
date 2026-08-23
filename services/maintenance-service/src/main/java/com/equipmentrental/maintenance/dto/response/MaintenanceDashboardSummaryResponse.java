package com.equipmentrental.maintenance.dto.response;

import java.math.BigDecimal;

public record MaintenanceDashboardSummaryResponse(

        long open,

        long assigned,

        long inProgress,

        long waitingParts,

        long overdue,

        long completed,

        long closed,

        BigDecimal totalCost
) {
}