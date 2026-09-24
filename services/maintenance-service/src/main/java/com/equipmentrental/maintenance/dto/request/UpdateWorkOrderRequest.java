package com.equipmentrental.maintenance.dto.request;

import com.equipmentrental.maintenance.enums.Severity;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateWorkOrderRequest(

        Severity priority,

        @Size(max = 255)
        String title,

        String description,

        String notes,

        LocalDateTime expectedStartAt,

        LocalDateTime expectedCompleteAt

) {
}