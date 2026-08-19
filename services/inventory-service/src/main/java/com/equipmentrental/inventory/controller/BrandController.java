package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.CreateBrandRequest;
import com.equipmentrental.inventory.dto.request.UpdateBrandRequest;
import com.equipmentrental.inventory.dto.response.BrandResponse;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
import com.equipmentrental.inventory.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService service;
    private final InventoryDataScopeGuard dataScopeGuard;

    @PostMapping
    @PreAuthorize("hasAuthority('inventory.brand.manage')")
    public ResponseEntity<BrandResponse> create(
            @Valid
            @RequestBody
            CreateBrandRequest request
    ) {
        dataScopeGuard.checkOrganization(request.organizationId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('inventory.brand.read')")
    public List<BrandResponse> getAll(
            @RequestParam Long organizationId
    ) {
        dataScopeGuard.checkOrganization(organizationId);
        return service.getAll(
                organizationId
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory.brand.read')")
    public BrandResponse getById(
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
    @PreAuthorize("hasAuthority('inventory.brand.manage')")
    public BrandResponse update(
            @PathVariable Long id,
            @RequestParam Long organizationId,
            @Valid
            @RequestBody
            UpdateBrandRequest request
    ) {
        dataScopeGuard.checkOrganization(organizationId);
        return service.update(
                id,
                organizationId,
                request
        );
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasAuthority('inventory.brand.manage')")
    public BrandResponse changeActive(
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
