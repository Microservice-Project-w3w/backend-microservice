package com.equipmentrental.maintenance.mapper;

import com.equipmentrental.maintenance.dto.response.WorkOrderResponse;
import com.equipmentrental.maintenance.dto.response.WorkOrderTimelineResponse;
import com.equipmentrental.maintenance.entity.MaintenanceWorkOrder;
import com.equipmentrental.maintenance.entity.WorkOrderTimeline;
import org.springframework.stereotype.Component;

@Component
public class WorkOrderMapper {

    public WorkOrderResponse toResponse(
            MaintenanceWorkOrder entity
    ) {

        return new WorkOrderResponse(

                entity.getId(),

                entity.getWorkOrderCode(),

                entity.getRequestId(),

                entity.getOrganizationId(),

                entity.getBranchId(),

                entity.getEquipmentId(),

                entity.getAssignedUserId(),

                entity.getStatus(),

                entity.getPriority(),

                entity.getTitle(),

                entity.getDescription(),

                entity.getDiagnosis(),

                entity.getActionTaken(),

                entity.getNotes(),

                entity.getExpectedStartAt(),

                entity.getExpectedCompleteAt(),

                entity.getActualStartAt(),

                entity.getActualCompleteAt(),

                entity.getClosedAt(),

                entity.getResult(),

                entity.getResultNotes(),

                entity.getCreatedByUserId(),

                entity.getCompletedByUserId(),

                entity.getClosedByUserId(),

                entity.getCancelledByUserId(),

                entity.getCancelledAt(),

                entity.getCancelReason(),

                entity.getCreatedAt(),

                entity.getUpdatedAt()
        );
    }

    public WorkOrderTimelineResponse
    toTimelineResponse(
            WorkOrderTimeline entity
    ) {

        return new WorkOrderTimelineResponse(

                entity.getId(),

                entity.getWorkOrderId(),

                entity.getEventType(),

                entity.getFromStatus(),

                entity.getToStatus(),

                entity.getActorUserId(),

                entity.getActorType(),

                entity.getMessage(),

                entity.getCreatedAt()
        );
    }
}