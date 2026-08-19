package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.CreateEquipmentRequest;
import com.equipmentrental.inventory.dto.response.EquipmentResponse;
import com.equipmentrental.inventory.enums.EquipmentStatus;
import com.equipmentrental.inventory.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.equipmentrental.inventory.dto.request.UpdateEquipmentRequest;
import com.equipmentrental.inventory.dto.request.UpdateEquipmentStatusRequest;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService service;
    private final InventoryDataScopeGuard dataScopeGuard;

    @PostMapping
    @PreAuthorize("hasAuthority('inventory.equipment.create')")
    public ResponseEntity<EquipmentResponse> create(
            @Valid
            @RequestBody
            CreateEquipmentRequest request
    ) {
        dataScopeGuard.checkBranch(request.organizationId(), request.branchId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('inventory.equipment.read')")
    public List<EquipmentResponse> getAll(
            @RequestParam Long organizationId,

            @RequestParam(required = false)
            Long branchId,

            @RequestParam(required = false)
            Long warehouseId,

            @RequestParam(required = false)
            Long modelId,

            @RequestParam(required = false)
            EquipmentStatus status
    ) {
        dataScopeGuard.checkOrganization(organizationId);
        if (branchId != null) {
            dataScopeGuard.checkReadableBranch(organizationId, branchId);
        }
        List<EquipmentResponse> values = service.getAll(
                organizationId,
                branchId,
                warehouseId,
                modelId,
                status
        );
        return dataScopeGuard.filterReadableBranches(
                organizationId,
                values,
                EquipmentResponse::branchId
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory.equipment.read')")
    public EquipmentResponse getById(
            @PathVariable Long id,

            @RequestParam Long organizationId
    ) {
        EquipmentResponse current = service.getById(
                id,
                organizationId
        );
        dataScopeGuard.checkReadableBranch(
                current.organizationId(),
                current.branchId()
        );
        return current;
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory.equipment.update')")
    public EquipmentResponse update(
            @PathVariable Long id,

            @RequestParam Long organizationId,

            @Valid
            @RequestBody
            UpdateEquipmentRequest request
    ) {
        EquipmentResponse current = service.getById(id, organizationId);
        dataScopeGuard.checkBranch(
                current.organizationId(),
                current.branchId()
        );
        dataScopeGuard.checkBranch(organizationId, request.branchId());
        return service.update(
                id,
                organizationId,
                request
        );
    }
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('inventory.equipment.change-status')")
    public EquipmentResponse changeStatus(
            @PathVariable Long id,

            @RequestParam Long organizationId,

            @Valid
            @RequestBody
            UpdateEquipmentStatusRequest request
    ) {
        EquipmentResponse current = service.getById(id, organizationId);
        dataScopeGuard.checkBranch(
                current.organizationId(),
                current.branchId()
        );
        return service.changeStatus(
                id,
                organizationId,
                request.status()
        );
    }
    @GetMapping("/by-serial/{serial}")
    @PreAuthorize("hasAuthority('inventory.equipment.read')")
    public EquipmentResponse getBySerial(
            @PathVariable String serial,
            @RequestParam Long organizationId
    ) {
        EquipmentResponse current = service.getBySerial(
                organizationId,
                serial
        );
        dataScopeGuard.checkReadableBranch(
                current.organizationId(),
                current.branchId()
        );
        return current;
    }

    @GetMapping("/by-imei/{imei}")
    @PreAuthorize("hasAuthority('inventory.equipment.read')")
    public EquipmentResponse getByImei(
            @PathVariable String imei,
            @RequestParam Long organizationId
    ) {
        EquipmentResponse current = service.getByImei(
                organizationId,
                imei
        );
        dataScopeGuard.checkReadableBranch(
                current.organizationId(),
                current.branchId()
        );
        return current;
    }

    @GetMapping("/by-mac/{mac}")
    @PreAuthorize("hasAuthority('inventory.equipment.read')")
    public EquipmentResponse getByMac(
            @PathVariable String mac,
            @RequestParam Long organizationId
    ) {
        EquipmentResponse current = service.getByMac(
                organizationId,
                mac
        );
        dataScopeGuard.checkReadableBranch(
                current.organizationId(),
                current.branchId()
        );
        return current;
    }
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('inventory.equipment.read')")
    public List<EquipmentResponse> search(

            @RequestParam Long organizationId,

            @RequestParam(required = false)
            Long branchId,

            @RequestParam(required = false)
            Long warehouseId,

            @RequestParam(required = false)
            Long categoryId,

            @RequestParam(required = false)
            Long equipmentTypeId,

            @RequestParam(required = false)
            Long brandId,

            @RequestParam(required = false)
            Long modelId,

            @RequestParam(required = false)
            EquipmentStatus status,

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            String serialNumber,

            @RequestParam(required = false)
            String imei,

            @RequestParam(required = false)
            String macAddress
    ) {
        dataScopeGuard.checkOrganization(organizationId);
        if (branchId != null) {
            dataScopeGuard.checkReadableBranch(organizationId, branchId);
        }
        List<EquipmentResponse> values = service.search(
                organizationId,
                branchId,
                warehouseId,
                categoryId,
                equipmentTypeId,
                brandId,
                modelId,
                status,
                keyword,
                serialNumber,
                imei,
                macAddress
        );
        return dataScopeGuard.filterReadableBranches(
                organizationId,
                values,
                EquipmentResponse::branchId
        );
    }
}
