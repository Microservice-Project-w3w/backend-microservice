package com.equipmentrental.maintenance.dto.response;

import com.equipmentrental.maintenance.enums.MaintenanceRequestStatus;
import com.equipmentrental.maintenance.enums.MaintenanceSourceType;
import com.equipmentrental.maintenance.enums.MaintenanceType;
import com.equipmentrental.maintenance.enums.Severity;

import java.time.LocalDateTime;

public record MaintenanceRequestResponse(

        Long id,
        String requestCode,

        Long organizationId,
        Long branchId,
        Long equipmentId,
        Long rentalOrderId,

        String sourceReferenceId,
        MaintenanceSourceType sourceType,

        MaintenanceType maintenanceType,
        Severity severity,

        String title,
        String description,

        MaintenanceRequestStatus status,

        Long reportedByUserId,
        Long reportedByCustomerId,

        Long cancelledByUserId,
        LocalDateTime cancelledAt,
        String cancelReason,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}