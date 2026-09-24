package com.equipmentrental.maintenance.dto.request;

import com.equipmentrental.maintenance.enums.MaintenanceSourceType;
import com.equipmentrental.maintenance.enums.MaintenanceType;
import com.equipmentrental.maintenance.enums.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMaintenanceRequestRequest(

        @NotNull
        Long branchId,

        @NotNull
        Long equipmentId,

        Long rentalOrderId,

        @Size(max = 100)
        String sourceReferenceId,

        MaintenanceSourceType sourceType,

        @NotNull
        MaintenanceType maintenanceType,

        @NotNull
        Severity severity,

        @NotBlank
        @Size(max = 255)
        String title,

        String description
) {
}