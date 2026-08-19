package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.response.EquipmentTransactionResponse;
import com.equipmentrental.inventory.dto.response.EquipmentResponse;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
import com.equipmentrental.inventory.service.EquipmentService;
import com.equipmentrental.inventory.service.EquipmentTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/inventory/equipment"
)
@RequiredArgsConstructor
public class EquipmentTransactionController {

    private final EquipmentTransactionService service;
    private final EquipmentService equipmentService;
    private final InventoryDataScopeGuard dataScopeGuard;

    @GetMapping("/{id}/transactions")
    @PreAuthorize("hasAuthority('inventory.equipment.read')")
    public List<EquipmentTransactionResponse> getHistory(
            @PathVariable Long id
    ) {
        EquipmentResponse equipment = equipmentService.getById(id);
        dataScopeGuard.checkReadableBranch(
                equipment.organizationId(),
                equipment.branchId()
        );

        return service.getHistory(id);
    }
}
