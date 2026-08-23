package com.equipmentrental.maintenance.dto.request;

import com.equipmentrental.maintenance.enums.InspectionItemResult;
import jakarta.validation.constraints.NotBlank;

public record InspectionChecklistItemRequest(

        String itemCode,

        @NotBlank
        String itemName,

        String expectedValue,

        String actualValue,

        InspectionItemResult itemResult,

        String note,

        Integer sortOrder
) {
}