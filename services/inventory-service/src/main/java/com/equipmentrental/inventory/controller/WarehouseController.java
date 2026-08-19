package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.CreateWarehouseRequest;
import com.equipmentrental.inventory.dto.request.UpdateWarehouseRequest;
import com.equipmentrental.inventory.dto.response.WarehouseResponse;
import com.equipmentrental.inventory.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService service;
    private final InventoryDataScopeGuard dataScopeGuard;

    @PostMapping
    @PreAuthorize("hasAuthority('inventory.warehouse.manage')")
    public WarehouseResponse create(
        @Valid @RequestBody CreateWarehouseRequest request
    ) {
        dataScopeGuard.checkBranch(
        request.organizationId(),
        request.branchId()
);
        return service.create(request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('inventory.warehouse.read')")
    public List<WarehouseResponse> findAll(
            @RequestParam
            Long organizationId,

            @RequestParam(required = false)
            Long branchId
    ) {
        if (branchId != null) {
        dataScopeGuard.checkBranch(
                organizationId,
                branchId
        );
    } else {
        dataScopeGuard.checkOrganization(
                organizationId
        );
    }
        List<WarehouseResponse> values = service.findAll(
                organizationId,
                branchId
        );
        return dataScopeGuard.filterAssignedBranches(
                organizationId,
                values,
                WarehouseResponse::branchId
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory.warehouse.read')")
    public WarehouseResponse findById(
            @PathVariable Long id
    ) {
        WarehouseResponse current =
            service.findById(id);

    dataScopeGuard.checkBranch(
            current.organizationId(),
            current.branchId()
    );
        return current;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory.warehouse.manage')")
    public WarehouseResponse update(
            @PathVariable Long id,
           @Valid@RequestBody UpdateWarehouseRequest request
    ) {
         WarehouseResponse current =
            service.findById(id);

    dataScopeGuard.checkBranch(
            current.organizationId(),
            current.branchId()
    );
        return service.update(
                id,
                request
        );
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasAuthority('inventory.warehouse.manage')")
    public WarehouseResponse changeActive(
            @PathVariable Long id,

            @RequestParam boolean active
    ) {
         WarehouseResponse current =
            service.findById(id);

    dataScopeGuard.checkBranch(
            current.organizationId(),
            current.branchId()
    );
        return service.changeActive(
                id,
                active
        );
    }
}
