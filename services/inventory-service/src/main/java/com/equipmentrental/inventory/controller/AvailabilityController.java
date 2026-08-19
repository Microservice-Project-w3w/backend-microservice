package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.response.EquipmentAvailabilityResponse;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
import com.equipmentrental.inventory.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/internal/equipment")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService service;
    private final InventoryDataScopeGuard dataScopeGuard;

    @GetMapping("/availability")
    @PreAuthorize("hasAuthority('inventory.availability.read')")
    public EquipmentAvailabilityResponse checkAvailability(
            @RequestParam Long organizationId,
            @RequestParam Long branchId,
            @RequestParam Long equipmentTypeId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startAt,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endAt,

            @RequestParam Integer quantity
    ) {
        dataScopeGuard.checkBranch(
                organizationId,
                branchId
        );

        return service.checkAvailability(
                organizationId,
                branchId,
                equipmentTypeId,
                startAt,
                endAt,
                quantity
        );
    }
}