package com.equipmentrental.maintenance.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignWorkOrderRequest(

        @NotNull
        Long assignedUserId

) {
}