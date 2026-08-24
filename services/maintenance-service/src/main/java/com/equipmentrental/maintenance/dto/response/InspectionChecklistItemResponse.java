package com.equipmentrental.maintenance.dto.response;

import com.equipmentrental.maintenance.enums.InspectionItemResult;

public record InspectionChecklistItemResponse(

        Long id,

        String itemCode,

        String itemName,

        String expectedValue,

        String actualValue,

        InspectionItemResult itemResult,

        String note,

        Integer sortOrder
) {
}