package com.equipmentrental.maintenance.dto.request;

import com.equipmentrental.maintenance.enums.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateWorkOrderRequest(

        Long requestId,

        @NotNull
        Long branchId,

        @NotNull
        Long equipmentId,

        @NotNull
        Severity priority,

        @NotBlank
        @Size(max = 255)
        String title,

        String description,

        LocalDateTime expectedStartAt,

        LocalDateTime expectedCompleteAt

) {
}