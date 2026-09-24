package com.equipmentrental.maintenance.dto.request;

import com.equipmentrental.maintenance.enums.InspectionCondition;
import com.equipmentrental.maintenance.enums.InspectionResult;
import com.equipmentrental.maintenance.enums.Severity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateInspectionRequest(

        @NotNull
        InspectionCondition condition,

        @NotNull
        Severity severity,

        @NotNull
        InspectionResult result,

        String cause,

        String recommendation,

        String notes,

        @Valid
        List<InspectionChecklistItemRequest> checklist

) {
}