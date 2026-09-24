package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.CheckoutEquipmentRequest;
import com.equipmentrental.inventory.dto.response.CheckoutEquipmentResponse;
import com.equipmentrental.inventory.dto.response.InternalEquipmentResponse;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
import com.equipmentrental.inventory.service.InternalEquipmentCheckoutService;
import com.equipmentrental.inventory.service.InternalEquipmentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/equipment")
@RequiredArgsConstructor
public class InternalEquipmentCheckoutController {

    private final InternalEquipmentCheckoutService service;
    private final InternalEquipmentQueryService queryService;
    private final InventoryDataScopeGuard dataScopeGuard;

    @PostMapping("/{id}/checkout")
    @PreAuthorize("hasAuthority('inventory.stock.out')")
    public CheckoutEquipmentResponse checkout(
            @PathVariable Long id,
            @RequestBody CheckoutEquipmentRequest request
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

        return service.checkout(
                id,
                request
        );
    }
}