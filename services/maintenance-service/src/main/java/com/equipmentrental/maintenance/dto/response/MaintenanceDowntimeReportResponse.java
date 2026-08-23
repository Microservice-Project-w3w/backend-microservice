package com.equipmentrental.maintenance.dto.response;

public record MaintenanceDowntimeReportResponse(

        Long equipmentId,

        long maintenanceCount,

        long downtimeMinutes
) {
}