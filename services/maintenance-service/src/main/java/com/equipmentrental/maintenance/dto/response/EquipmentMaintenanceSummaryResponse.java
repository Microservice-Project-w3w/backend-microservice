package com.equipmentrental.maintenance.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EquipmentMaintenanceSummaryResponse(

        Long equipmentId,

        long totalWorkOrders,

        long openWorkOrders,

        long completedWorkOrders,

        long closedWorkOrders,

        long totalDowntimeMinutes,

        BigDecimal totalMaintenanceCost,

        LocalDateTime lastMaintenanceAt
) {
}