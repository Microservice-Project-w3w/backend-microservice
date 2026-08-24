package com.equipmentrental.maintenance.mapper;

import com.equipmentrental.maintenance.dto.response.MaintenanceRequestResponse;
import com.equipmentrental.maintenance.dto.response.WorkOrderSummaryResponse;
import com.equipmentrental.maintenance.entity.MaintenanceRequest;
import com.equipmentrental.maintenance.entity.MaintenanceWorkOrder;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceRequestMapper {

    public MaintenanceRequestResponse toResponse(
            MaintenanceRequest entity
    ) {

        return new MaintenanceRequestResponse(
                entity.getId(),
                entity.getRequestCode(),

                entity.getOrganizationId(),
                entity.getBranchId(),
                entity.getEquipmentId(),
                entity.getRentalOrderId(),

                entity.getSourceReferenceId(),
                entity.getSourceType(),

                entity.getMaintenanceType(),
                entity.getSeverity(),

                entity.getTitle(),
                entity.getDescription(),

                entity.getStatus(),

                entity.getReportedByUserId(),
                entity.getReportedByCustomerId(),

                entity.getCancelledByUserId(),
                entity.getCancelledAt(),
                entity.getCancelReason(),

                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public WorkOrderSummaryResponse toWorkOrderResponse(
            MaintenanceWorkOrder entity
    ) {

        return new WorkOrderSummaryResponse(
                entity.getId(),
                entity.getWorkOrderCode(),
                entity.getRequestId(),

                entity.getEquipmentId(),
                entity.getBranchId(),

                entity.getStatus(),
                entity.getPriority(),

                entity.getTitle(),

                entity.getCreatedAt()
        );
    }
}