package com.equipmentrental.maintenance.dto.request;

import com.equipmentrental.maintenance.enums.WorkOrderResult;
import jakarta.validation.constraints.NotNull;

public record CompleteWorkOrderRequest(

        @NotNull
        WorkOrderResult result,

        String diagnosis,

        String actionTaken,

        String resultNotes

) {
}