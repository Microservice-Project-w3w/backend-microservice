package com.equipmentrental.inventory.controller;


import com.equipmentrental.inventory.dto.request.CreateEquipmentImageRequest;
import com.equipmentrental.inventory.dto.response.EquipmentImageResponse;
import com.equipmentrental.inventory.dto.response.EquipmentResponse;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
import com.equipmentrental.inventory.service.EquipmentService;
import com.equipmentrental.inventory.service.EquipmentImageService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;


@RestController
@RequestMapping("/api/v1/inventory/equipment-images")
@RequiredArgsConstructor
public class EquipmentImageController {


    private final EquipmentImageService service;
    private final EquipmentService equipmentService;
    private final InventoryDataScopeGuard dataScopeGuard;



    @PostMapping
    @PreAuthorize("hasAuthority('inventory.equipment.image.manage')")
    public ResponseEntity<?> create(
            @RequestBody CreateEquipmentImageRequest request
    ){
        checkEquipmentWriteScope(request.equipmentId());

        return ResponseEntity.ok(
                service.create(request)
        );
    }



    @GetMapping("/equipment/{equipmentId}")
    @PreAuthorize("hasAuthority('inventory.equipment.read')")
    public ResponseEntity<?> getByEquipment(
            @PathVariable Long equipmentId
    ){
        checkEquipmentReadScope(equipmentId);

        return ResponseEntity.ok(
                service.getByEquipment(equipmentId)
        );
    }



    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory.equipment.image.manage')")
    public ResponseEntity<?> delete(
            @PathVariable Long id
    ){
        EquipmentImageResponse image = service.getById(id);
        checkEquipmentWriteScope(image.equipmentId());

        service.delete(id);

        return ResponseEntity.noContent()
                .build();
    }

    private void checkEquipmentReadScope(Long equipmentId) {
        EquipmentResponse equipment = equipmentService.getById(equipmentId);
        dataScopeGuard.checkReadableBranch(
                equipment.organizationId(),
                equipment.branchId()
        );
    }

    private void checkEquipmentWriteScope(Long equipmentId) {
        EquipmentResponse equipment = equipmentService.getById(equipmentId);
        dataScopeGuard.checkBranch(
                equipment.organizationId(),
                equipment.branchId()
        );
    }

}
