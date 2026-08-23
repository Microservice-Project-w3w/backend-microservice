package com.equipmentrental.maintenance.dto.response;

import com.equipmentrental.maintenance.enums.InspectionCondition;
import com.equipmentrental.maintenance.enums.InspectionResult;
import com.equipmentrental.maintenance.enums.InspectionStatus;
import com.equipmentrental.maintenance.enums.Severity;

import java.time.LocalDateTime;
import java.util.List;

public record InspectionResponse(

        Long id,

        Long workOrderId,

        Long organizationId,

        Long branchId,

        Long equipmentId,

        InspectionCondition condition,

        Severity severity,

        InspectionResult result,

        String cause,

        String recommendation,

        String notes,

        InspectionStatus status,

        Long inspectedByUserId,

        LocalDateTime inspectedAt,

        Long submittedByUserId,

        LocalDateTime submittedAt,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        List<InspectionChecklistItemResponse> checklist
) {
}