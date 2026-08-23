package com.equipmentrental.maintenance.dto.request;

import com.equipmentrental.maintenance.enums.MaintenanceType;
import com.equipmentrental.maintenance.enums.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InternalCreateMaintenanceRequest(

        @NotNull
        Long organizationId,

        @NotNull
        Long branchId,

        @NotNull
        Long equipmentId,

        Long rentalOrderId,

        @NotBlank
        String sourceReferenceId,

        @NotNull
        MaintenanceType maintenanceType,

        @NotNull
        Severity severity,

        @NotBlank
        String title,

        String description

) {
}