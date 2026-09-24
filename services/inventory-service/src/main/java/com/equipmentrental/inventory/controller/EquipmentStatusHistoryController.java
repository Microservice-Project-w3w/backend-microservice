package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.CreateStatusHistoryRequest;
import com.equipmentrental.inventory.dto.response.EquipmentStatusHistoryResponse;
import com.equipmentrental.inventory.dto.response.InternalEquipmentResponse;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
import com.equipmentrental.inventory.service.EquipmentStatusHistoryService;
import com.equipmentrental.inventory.service.InternalEquipmentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/inventory/equipment/{equipmentId}/status-history"
)
@RequiredArgsConstructor
public class EquipmentStatusHistoryController {

    private final EquipmentStatusHistoryService service;
    private final InternalEquipmentQueryService queryService;
    private final InventoryDataScopeGuard dataScopeGuard;

    @PostMapping
    @PreAuthorize("hasAuthority('inventory.equipment.change-status')")
    public EquipmentStatusHistoryResponse create(
            @PathVariable Long equipmentId,
            @RequestBody CreateStatusHistoryRequest request
    ) {
        InternalEquipmentResponse equipment =
                queryService.findById(equipmentId);

        dataScopeGuard.checkBranch(
                equipment.organizationId(),
                equipment.branchId()
        );

        Long changedBy =
                Long.valueOf(
                        dataScopeGuard.getCurrentUserId()
                );

        return service.create(
                equipmentId,
                request,
                changedBy
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('inventory.equipment.read')")
    public List<EquipmentStatusHistoryResponse> findAll(
            @PathVariable Long equipmentId
    ) {
        InternalEquipmentResponse equipment =
                queryService.findById(equipmentId);

        dataScopeGuard.checkBranch(
                equipment.organizationId(),
                equipment.branchId()
        );

        return service.findAll(
                equipmentId
        );
    }
}