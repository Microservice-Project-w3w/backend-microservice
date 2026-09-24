package com.equipmentrental.maintenance.dto.request;

import com.equipmentrental.maintenance.enums.MaintenanceType;
import com.equipmentrental.maintenance.enums.Severity;
import jakarta.validation.constraints.Size;

public record UpdateMaintenanceRequestRequest(

        MaintenanceType maintenanceType,

        Severity severity,

        @Size(max = 255)
        String title,

        String description
) {
}