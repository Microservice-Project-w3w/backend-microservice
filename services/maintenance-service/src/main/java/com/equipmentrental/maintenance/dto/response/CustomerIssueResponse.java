package com.equipmentrental.maintenance.dto.response;

import com.equipmentrental.maintenance.enums.CustomerIssueStatus;
import com.equipmentrental.maintenance.enums.Severity;

import java.time.LocalDateTime;

public record CustomerIssueResponse(

        Long id,
        String issueCode,

        Long organizationId,
        Long branchId,

        Long customerId,
        Long equipmentId,
        Long rentalOrderId,

        String title,
        String description,

        Severity severity,
        CustomerIssueStatus status,

        Long maintenanceRequestId,
        Long workOrderId,

        LocalDateTime reportedAt,
        LocalDateTime resolvedAt,
        String resolutionNote,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}