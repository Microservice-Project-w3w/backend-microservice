package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.CheckinEquipmentRequest;
import com.equipmentrental.inventory.dto.response.CheckinEquipmentResponse;
import com.equipmentrental.inventory.dto.response.InternalEquipmentResponse;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
import com.equipmentrental.inventory.service.InternalEquipmentCheckinService;
import com.equipmentrental.inventory.service.InternalEquipmentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/equipment")
@RequiredArgsConstructor
public class InternalEquipmentCheckinController {

    private final InternalEquipmentCheckinService service;
    private final InternalEquipmentQueryService queryService;
    private final InventoryDataScopeGuard dataScopeGuard;

    @PostMapping("/{id}/checkin")
    @PreAuthorize("hasAuthority('inventory.stock.in')")
    public CheckinEquipmentResponse checkin(
            @PathVariable Long id,
            @RequestBody CheckinEquipmentRequest request
    ) {
        dataScopeGuard.checkBranch(
                request.organizationId(),
                request.branchId()
        );

        InternalEquipmentResponse equipment =
                queryService.findById(id);

        dataScopeGuard.checkBranch(
                equipment.organizationId(),
                equipment.branchId()
        );

        return service.checkin(
                id,
                request
        );
    }
}