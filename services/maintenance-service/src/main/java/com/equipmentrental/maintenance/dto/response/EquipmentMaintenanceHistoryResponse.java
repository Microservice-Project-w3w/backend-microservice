package com.equipmentrental.maintenance.dto.response;

import com.equipmentrental.maintenance.enums.WorkOrderResult;
import com.equipmentrental.maintenance.enums.WorkOrderStatus;
import com.equipmentrental.maintenance.enums.Severity;

import java.time.LocalDateTime;

public record EquipmentMaintenanceHistoryResponse(

        Long workOrderId,

        String workOrderCode,

        Long equipmentId,

        Long branchId,

        String title,

        WorkOrderStatus status,

        Severity priority,

        String diagnosis,

        String actionTaken,

        WorkOrderResult result,

        LocalDateTime actualStartAt,

        LocalDateTime actualCompleteAt,

        LocalDateTime createdAt
) {
}