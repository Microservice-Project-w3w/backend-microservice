package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.CreateEquipmentCategoryRequest;
import com.equipmentrental.inventory.dto.request.UpdateEquipmentCategoryRequest;
import com.equipmentrental.inventory.dto.response.EquipmentCategoryResponse;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
import com.equipmentrental.inventory.service.EquipmentCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/categories")
@RequiredArgsConstructor
public class EquipmentCategoryController {

    private final EquipmentCategoryService service;
    private final InventoryDataScopeGuard dataScopeGuard;


    @PostMapping
    @PreAuthorize("hasAuthority('inventory.catalog.manage')")
    public ResponseEntity<EquipmentCategoryResponse> create(
            @Valid @RequestBody CreateEquipmentCategoryRequest request
    ) {
        dataScopeGuard.checkOrganization(request.organizationId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('inventory.catalog.read')")
    public List<EquipmentCategoryResponse> getAll(
            @RequestParam Long organizationId
    ) {
        dataScopeGuard.checkOrganization(organizationId);
        return service.getAll(organizationId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory.catalog.read')")
    public EquipmentCategoryResponse getById(
            @PathVariable Long id,
            @RequestParam Long organizationId
    ) {
        dataScopeGuard.checkOrganization(organizationId);
        return service.getById(id, organizationId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory.catalog.manage')")
    public EquipmentCategoryResponse update(
            @PathVariable Long id,
            @RequestParam Long organizationId,
            @Valid @RequestBody UpdateEquipmentCategoryRequest request
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
    public EquipmentCategoryResponse changeActive(
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
