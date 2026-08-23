package com.equipmentrental.maintenance.dto.response;

import com.equipmentrental.maintenance.enums.WorkOrderStatus;
import com.equipmentrental.maintenance.enums.Severity;

import java.time.LocalDateTime;

public record OpenWorkOrderResponse(

        Long id,

        String workOrderCode,

        Long equipmentId,

        Long branchId,

        String title,

        WorkOrderStatus status,

        Severity priority,

        Long assignedUserId,

        LocalDateTime expectedStartAt,

        LocalDateTime expectedCompleteAt,

        LocalDateTime actualStartAt
) {
}