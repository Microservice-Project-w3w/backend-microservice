package com.equipmentrental.maintenance.dto.request;

import com.equipmentrental.maintenance.enums.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCustomerIssueRequest(

        @NotNull
        Long rentalOrderId,

        @NotNull
        Long equipmentId,

        @NotBlank
        String title,

        @NotBlank
        String description,

        Severity severity
) {
}