package com.equipmentrental.maintenance.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PartUsageResponse(

        Long id,
        Long workOrderId,
        Long partId,
        String partCodeSnapshot,
        String partNameSnapshot,
        BigDecimal quantity,
        String unit,
        BigDecimal unitCost,
        BigDecimal totalCost,
        String note,
        Long createdByUserId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}