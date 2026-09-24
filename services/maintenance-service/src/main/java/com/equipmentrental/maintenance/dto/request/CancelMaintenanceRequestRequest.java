package com.equipmentrental.maintenance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelMaintenanceRequestRequest(

        @NotBlank
        @Size(max = 500)
        String reason

) {
}