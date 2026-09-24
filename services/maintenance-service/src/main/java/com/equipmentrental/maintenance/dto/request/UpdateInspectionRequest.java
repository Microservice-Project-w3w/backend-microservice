package com.equipmentrental.maintenance.dto.request;

import com.equipmentrental.maintenance.enums.InspectionCondition;
import com.equipmentrental.maintenance.enums.InspectionResult;
import com.equipmentrental.maintenance.enums.Severity;
import jakarta.validation.Valid;

import java.util.List;

public record UpdateInspectionRequest(

        InspectionCondition condition,

        Severity severity,

        InspectionResult result,

        String cause,

        String recommendation,

        String notes,

        @Valid
        List<InspectionChecklistItemRequest> checklist

) {
}