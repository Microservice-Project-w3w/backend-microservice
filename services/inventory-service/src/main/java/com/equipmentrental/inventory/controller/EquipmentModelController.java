package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.CreateEquipmentModelRequest;
import com.equipmentrental.inventory.dto.request.UpdateEquipmentModelRequest;
import com.equipmentrental.inventory.dto.response.EquipmentModelResponse;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
import com.equipmentrental.inventory.service.EquipmentModelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/models")
@RequiredArgsConstructor
public class EquipmentModelController {

    private final EquipmentModelService service;
    private final InventoryDataScopeGuard dataScopeGuard;

    @PostMapping
    @PreAuthorize("hasAuthority('inventory.model.manage')")
    public ResponseEntity<EquipmentModelResponse> create(
            @Valid
            @RequestBody
            CreateEquipmentModelRequest request
    ) {
        dataScopeGuard.checkOrganization(request.organizationId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('inventory.model.read')")
    public List<EquipmentModelResponse> getAll(
            @RequestParam Long organizationId,

            @RequestParam(required = false)
            Long equipmentTypeId,

            @RequestParam(required = false)
            Long brandId
    ) {
        dataScopeGuard.checkOrganization(organizationId);
        return service.getAll(
                organizationId,
                equipmentTypeId,
                brandId
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory.model.read')")
    public EquipmentModelResponse getById(
            @PathVariable Long id,

            @RequestParam Long organizationId
    ) {
        dataScopeGuard.checkOrganization(organizationId);
        return service.getById(
                id,
                organizationId
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory.model.manage')")
    public EquipmentModelResponse update(
            @PathVariable Long id,

            @RequestParam Long organizationId,

            @Valid
            @RequestBody
            UpdateEquipmentModelRequest request
    ) {
        dataScopeGuard.checkOrganization(organizationId);
        return service.update(
                id,
                organizationId,
                request
        );
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasAuthority('inventory.model.manage')")
    public EquipmentModelResponse changeActive(
            @PathVariable Long id,

            @RequestParam Long organizationId,

            @RequestParam boolean active
    ) {
        dataScopeGuard.checkOrganization(organizationId);
        return service.changeActive(
                id,
                organizationId,
                active
        );
    }
}
