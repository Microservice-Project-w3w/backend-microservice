package com.equipmentrental.inventory.controller;


import com.equipmentrental.inventory.dto.request.*;
import com.equipmentrental.inventory.dto.response.EquipmentResponse;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
import com.equipmentrental.inventory.service.EquipmentAccessoryService;
import com.equipmentrental.inventory.service.EquipmentService;


import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping(
        "/api/v1/inventory/equipment/{equipmentId}/accessories"
)
@RequiredArgsConstructor
public class EquipmentAccessoryController {


    private final EquipmentAccessoryService service;
    private final EquipmentService equipmentService;
    private final InventoryDataScopeGuard dataScopeGuard;



    @PostMapping
    @PreAuthorize("hasAuthority('inventory.equipment.accessory.manage')")
    public ResponseEntity<?> create(

            @PathVariable Long equipmentId,

            @RequestBody CreateEquipmentAccessoryRequest request

    ){
        checkEquipmentWriteScope(equipmentId);

        return ResponseEntity.ok(
                service.create(
                        equipmentId,
                        request
                )
        );

    }



    @GetMapping
    @PreAuthorize("hasAuthority('inventory.equipment.read')")
    public ResponseEntity<?> findAll(

            @PathVariable Long equipmentId

    ){
        checkEquipmentReadScope(equipmentId);

        return ResponseEntity.ok(
                service.findAll(
                        equipmentId
                )
        );

    }




    @GetMapping("/{accessoryId}")
    @PreAuthorize("hasAuthority('inventory.equipment.read')")
    public ResponseEntity<?> findById(

            @PathVariable Long equipmentId,

            @PathVariable Long accessoryId

    ){
        checkEquipmentReadScope(equipmentId);

        return ResponseEntity.ok(
                service.findById(
                        equipmentId,
                        accessoryId
                )
        );

    }





    @PutMapping("/{accessoryId}")
    @PreAuthorize("hasAuthority('inventory.equipment.accessory.manage')")
    public ResponseEntity<?> update(

            @PathVariable Long equipmentId,

            @PathVariable Long accessoryId,

            @RequestBody UpdateEquipmentAccessoryRequest request

    ){
        checkEquipmentWriteScope(equipmentId);

        return ResponseEntity.ok(
                service.update(
                        equipmentId,
                        accessoryId,
                        request
                )
        );

    }





    @DeleteMapping("/{accessoryId}")
    @PreAuthorize("hasAuthority('inventory.equipment.accessory.manage')")
    public ResponseEntity<?> delete(

            @PathVariable Long equipmentId,

            @PathVariable Long accessoryId

    ){
        checkEquipmentWriteScope(equipmentId);

        service.delete(
                equipmentId,
                accessoryId
        );


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
