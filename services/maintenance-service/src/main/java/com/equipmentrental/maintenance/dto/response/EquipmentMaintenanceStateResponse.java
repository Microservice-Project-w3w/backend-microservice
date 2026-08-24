package com.equipmentrental.maintenance.dto.response;

public record EquipmentMaintenanceStateResponse(

        Long equipmentId,

        boolean underMaintenance,

        String requestStatus,

        String workOrderStatus

) {
}