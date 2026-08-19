package com.equipmentrental.inventory.controller;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
import com.equipmentrental.inventory.dto.response.InternalEquipmentResponse;
import com.equipmentrental.inventory.service.InternalEquipmentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController
@RequestMapping("/internal/equipment")
@RequiredArgsConstructor
public class InternalEquipmentQueryController {

    private final InternalEquipmentQueryService service;
    private final InventoryDataScopeGuard dataScopeGuard;
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory.equipment.read')")
    public InternalEquipmentResponse findById(
            @PathVariable Long id
    ) {

       InternalEquipmentResponse current =
            service.findById(id);

    dataScopeGuard.checkBranch(
            current.organizationId(),
            current.branchId()
    );

    return current;
    }
}