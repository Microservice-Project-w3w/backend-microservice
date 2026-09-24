package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.CompleteStockAuditRequest;
import com.equipmentrental.inventory.dto.request.CreateStockAuditRequest;
import com.equipmentrental.inventory.dto.request.RecordStockAuditItemRequest;
import com.equipmentrental.inventory.dto.request.StartStockAuditRequest;
import com.equipmentrental.inventory.dto.response.StockAuditResponse;
import com.equipmentrental.inventory.dto.response.WarehouseResponse;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
import com.equipmentrental.inventory.service.StockAuditService;
import com.equipmentrental.inventory.service.WarehouseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize("hasAuthority('inventory.stock.audit')")
@RequestMapping("/api/v1/inventory/stock-audits")
@RequiredArgsConstructor
public class StockAuditController {

    private final StockAuditService service;
    private final InventoryDataScopeGuard dataScopeGuard;
    private final WarehouseService warehouseService;

    @PostMapping
    public ResponseEntity<StockAuditResponse> create(
            @Valid
            @RequestBody
            CreateStockAuditRequest request
    ) {
        dataScopeGuard.checkBranch(
                request.organizationId(),
                request.branchId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        service.create(request)
                );
    }

    @GetMapping
    public List<StockAuditResponse> findAll(
            @RequestParam
            Long organizationId,

            @RequestParam(required = false)
            Long branchId,

            @RequestParam(required = false)
            Long warehouseId
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

        if (warehouseId != null) {
            WarehouseResponse warehouse =
                    warehouseService.findById(
                            warehouseId
                    );

            dataScopeGuard.checkBranch(
                    warehouse.organizationId(),
                    warehouse.branchId()
            );

            if (!warehouse.organizationId()
                    .equals(organizationId)) {
                throw new IllegalArgumentException(
                        "Warehouse does not belong to organization"
                );
            }

            if (branchId != null
                    && !warehouse.branchId()
                    .equals(branchId)) {
                throw new IllegalArgumentException(
                        "Warehouse does not belong to branch"
                );
            }
        }

        List<StockAuditResponse> values = service.findAll(
                organizationId,
                branchId,
                warehouseId
        );
        return dataScopeGuard.filterAssignedBranches(
                organizationId,
                values,
                StockAuditResponse::branchId
        );
    }

    @GetMapping("/{id}")
    public StockAuditResponse findById(
            @PathVariable Long id
    ) {
        StockAuditResponse current =
                service.findById(id);

        dataScopeGuard.checkBranch(
                current.organizationId(),
                current.branchId()
        );

        return current;
    }

    @PostMapping("/{id}/start")
    public StockAuditResponse start(
            @PathVariable Long id,

            @RequestBody(required = false)
            StartStockAuditRequest request
    ) {
        StockAuditResponse current =
                service.findById(id);

        dataScopeGuard.checkBranch(
                current.organizationId(),
                current.branchId()
        );

        return service.start(
                id,
                request
        );
    }

    @PostMapping("/{id}/items")
    public StockAuditResponse recordItem(
            @PathVariable Long id,

            @Valid
            @RequestBody
            RecordStockAuditItemRequest request
    ) {
        StockAuditResponse current =
                service.findById(id);

        dataScopeGuard.checkBranch(
                current.organizationId(),
                current.branchId()
        );

        return service.recordItem(
                id,
                request
        );
    }

    @PostMapping("/{id}/complete")
    public StockAuditResponse complete(
            @PathVariable Long id,

            @RequestBody(required = false)
            CompleteStockAuditRequest request
    ) {
        StockAuditResponse current =
                service.findById(id);

        dataScopeGuard.checkBranch(
                current.organizationId(),
                current.branchId()
        );

        return service.complete(
                id,
                request
        );
    }

    @PostMapping("/{id}/cancel")
    public StockAuditResponse cancel(
            @PathVariable Long id
    ) {
        StockAuditResponse current =
                service.findById(id);

        dataScopeGuard.checkBranch(
                current.organizationId(),
                current.branchId()
        );

        return service.cancel(id);
    }
}
