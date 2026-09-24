package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.CreateEquipmentTypeRequest;
import com.equipmentrental.inventory.dto.request.UpdateEquipmentTypeRequest;
import com.equipmentrental.inventory.dto.response.EquipmentTypeResponse;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
import com.equipmentrental.inventory.service.EquipmentTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/equipment-types")
@RequiredArgsConstructor
public class EquipmentTypeController {

    private final EquipmentTypeService service;
    private final InventoryDataScopeGuard dataScopeGuard;

    @PostMapping
    @PreAuthorize("hasAuthority('inventory.catalog.manage')")
    public ResponseEntity<EquipmentTypeResponse> create(
            @Valid
            @RequestBody
            CreateEquipmentTypeRequest request
    ) {
        dataScopeGuard.checkOrganization(request.organizationId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('inventory.catalog.read')")
    public List<EquipmentTypeResponse> getAll(
            @RequestParam Long organizationId,

            @RequestParam(required = false)
            Long categoryId
    ) {
        dataScopeGuard.checkOrganization(organizationId);
        return service.getAll(
                organizationId,
                categoryId
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory.catalog.read')")
    public EquipmentTypeResponse getById(
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
    @PreAuthorize("hasAuthority('inventory.catalog.manage')")
    public EquipmentTypeResponse update(
            @PathVariable Long id,

            @RequestParam Long organizationId,

            @Valid
            @RequestBody
            UpdateEquipmentTypeRequest request
    ) {
        dataScopeGuard.checkOrganization(organizationId);
        return service.update(
                id,
                organizationId,
                request
        );
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasAuthority('inventory.catalog.manage')")
    public EquipmentTypeResponse changeActive(
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
