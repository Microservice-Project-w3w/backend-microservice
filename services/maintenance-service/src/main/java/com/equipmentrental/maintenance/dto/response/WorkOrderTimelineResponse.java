package com.equipmentrental.maintenance.dto.response;

import java.time.LocalDateTime;

public record WorkOrderTimelineResponse(

        Long id,

        Long workOrderId,

        String eventType,

        String fromStatus,

        String toStatus,

        Long actorUserId,

        String actorType,

        String message,

        LocalDateTime createdAt

) {
}