package com.equipmentrental.maintenance.dto.response;

import com.equipmentrental.maintenance.enums.Severity;
import com.equipmentrental.maintenance.enums.WorkOrderResult;
import com.equipmentrental.maintenance.enums.WorkOrderStatus;

import java.time.LocalDateTime;

public record WorkOrderResponse(

        Long id,

        String workOrderCode,

        Long requestId,

        Long organizationId,

        Long branchId,

        Long equipmentId,

        Long assignedUserId,

        WorkOrderStatus status,

        Severity priority,

        String title,

        String description,

        String diagnosis,

        String actionTaken,

        String notes,

        LocalDateTime expectedStartAt,

        LocalDateTime expectedCompleteAt,

        LocalDateTime actualStartAt,

        LocalDateTime actualCompleteAt,

        LocalDateTime closedAt,

        WorkOrderResult result,

        String resultNotes,

        Long createdByUserId,

        Long completedByUserId,

        Long closedByUserId,

        Long cancelledByUserId,

        LocalDateTime cancelledAt,

        String cancelReason,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}