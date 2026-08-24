package com.equipmentrental.maintenance.dto.response;

import com.equipmentrental.maintenance.enums.PreventiveFrequencyType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PreventivePlanResponse(

        Long id,

        String planCode,

        Long organizationId,

        Long branchId,

        Long equipmentId,

        Long equipmentTypeId,

        String name,

        String description,

        PreventiveFrequencyType frequencyType,

        Integer frequencyValue,

        LocalDate lastMaintenanceDate,

        LocalDate nextMaintenanceDate,

        Boolean active,

        Long createdByUserId,

        Long updatedByUserId,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}