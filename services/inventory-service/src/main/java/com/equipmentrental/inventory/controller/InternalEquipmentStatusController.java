package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.ChangeEquipmentStatusRequest;
import com.equipmentrental.inventory.entity.Equipment;
import com.equipmentrental.inventory.repository.EquipmentRepository;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
import com.equipmentrental.inventory.service.InternalEquipmentStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/equipment")
@RequiredArgsConstructor
public class InternalEquipmentStatusController {

    private final InternalEquipmentStatusService statusService;
    private final EquipmentRepository equipmentRepository;
    private final InventoryDataScopeGuard dataScopeGuard;

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('inventory.equipment.read')")
    public void changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeEquipmentStatusRequest request
    ) {

        Equipment equipment = equipmentRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy thiết bị id=" + id
                        )
                );

        dataScopeGuard.checkBranch(
                equipment.getOrganizationId(),
                equipment.getBranchId()
        );

        statusService.changeStatus(
                id,
                request.status()
        );
    }
}