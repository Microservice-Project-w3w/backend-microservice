package com.equipmentrental.maintenance.dto.request;

import com.equipmentrental.maintenance.enums.PreventiveFrequencyType;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;

public record UpdatePreventivePlanRequest(

        String name,

        String description,

        PreventiveFrequencyType frequencyType,

        @Min(1)
        Integer frequencyValue,

        LocalDate nextMaintenanceDate
) {
}