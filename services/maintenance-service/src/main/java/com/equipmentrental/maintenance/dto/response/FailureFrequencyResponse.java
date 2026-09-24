package com.equipmentrental.maintenance.dto.response;

public record FailureFrequencyResponse(

        Long equipmentId,

        long failureCount
) {
}