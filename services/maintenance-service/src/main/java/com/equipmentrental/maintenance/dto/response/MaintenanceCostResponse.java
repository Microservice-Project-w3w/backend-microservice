package com.equipmentrental.maintenance.dto.response;

import com.equipmentrental.maintenance.enums.CostApprovalStatus;
import com.equipmentrental.maintenance.enums.CostType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MaintenanceCostResponse(

        Long id,
        Long workOrderId,
        Long organizationId,
        Long branchId,
        CostType costType,
        BigDecimal amount,
        String currencyCode,
        String description,
        CostApprovalStatus approvalStatus,
        Long approvedByUserId,
        LocalDateTime approvedAt,
        String approvalNote,
        Long createdByUserId,
        Long updatedByUserId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}