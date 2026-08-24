package com.equipmentrental.maintenance.dto.response;

import com.equipmentrental.maintenance.enums.Severity;
import com.equipmentrental.maintenance.enums.WorkOrderStatus;

import java.time.LocalDateTime;

public record WorkOrderSummaryResponse(

        Long id,
        String workOrderCode,
        Long requestId,

        Long equipmentId,
        Long branchId,

        WorkOrderStatus status,
        Severity priority,

        String title,

        LocalDateTime createdAt
) {
}