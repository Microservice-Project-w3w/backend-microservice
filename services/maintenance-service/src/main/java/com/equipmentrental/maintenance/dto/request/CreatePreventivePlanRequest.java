package com.equipmentrental.maintenance.dto.request;

import com.equipmentrental.maintenance.enums.PreventiveFrequencyType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreatePreventivePlanRequest(

        Long branchId,

        Long equipmentId,

        Long equipmentTypeId,

        @NotBlank
        String name,

        String description,

        @NotNull
        PreventiveFrequencyType frequencyType,

        @NotNull
        @Min(1)
        Integer frequencyValue,

        @NotNull
        LocalDate nextMaintenanceDate
) {
}