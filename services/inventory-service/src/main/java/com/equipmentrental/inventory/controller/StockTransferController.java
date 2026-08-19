package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.ApproveStockTransferRequest;
import com.equipmentrental.inventory.dto.request.CreateStockTransferRequest;
import com.equipmentrental.inventory.dto.request.ReceiveStockTransferRequest;
import com.equipmentrental.inventory.dto.response.StockTransferResponse;
import com.equipmentrental.inventory.dto.response.WarehouseResponse;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
import com.equipmentrental.inventory.service.StockTransferService;
import com.equipmentrental.inventory.service.WarehouseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize("hasAuthority('inventory.stock.transfer')")
@RequestMapping("/api/v1/inventory/transfers")
@RequiredArgsConstructor
public class StockTransferController {

    private final StockTransferService service;
    private final InventoryDataScopeGuard dataScopeGuard;
    private final WarehouseService warehouseService;

    @PostMapping
    public ResponseEntity<StockTransferResponse> create(
            @Valid
            @RequestBody
            CreateStockTransferRequest request
    ) {
        WarehouseResponse sourceWarehouse =
                warehouseService.findById(
                        request.sourceWarehouseId()
                );

        WarehouseResponse destinationWarehouse =
                warehouseService.findById(
                        request.destinationWarehouseId()
                );

        if (!sourceWarehouse.organizationId()
                .equals(request.organizationId())) {

            throw new IllegalArgumentException(
                    "Source warehouse does not belong to organization"
            );
        }

        if (!destinationWarehouse.organizationId()
                .equals(request.organizationId())) {

            throw new IllegalArgumentException(
                    "Destination warehouse does not belong to organization"
            );
        }

        dataScopeGuard.checkBranch(
                request.organizationId(),
                sourceWarehouse.branchId()
        );

        dataScopeGuard.checkBranch(
                request.organizationId(),
                destinationWarehouse.branchId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        service.create(request)
                );
    }

    @GetMapping
    public List<StockTransferResponse> findAll(
            @RequestParam Long organizationId,
            @RequestParam(required = false) Long branchId
    ) {
        if (branchId != null) {

            dataScopeGuard.checkBranch(
                    organizationId,
                    branchId
            );

            return service.findAll(
                    organizationId,
                    branchId
            );
        }

        dataScopeGuard.checkOrganization(
                organizationId
        );

        List<StockTransferResponse> transfers =
                service.findAll(
                        organizationId,
                        null
                );

        if (dataScopeGuard.isAdmin()) {
            return transfers;
        }

        return transfers
                .stream()
                .filter(transfer ->
                        dataScopeGuard.canAccessBranch(
                                organizationId,
                                transfer.fromBranchId()
                        )
                                &&
                        dataScopeGuard.canAccessBranch(
                                organizationId,
                                transfer.toBranchId()
                        )
                )
                .toList();
    }

    @GetMapping("/{id}")
    public StockTransferResponse findById(
            @PathVariable Long id
    ) {
        StockTransferResponse current =
                service.findById(id);

        dataScopeGuard.checkBranch(
                current.organizationId(),
                current.fromBranchId()
        );

        dataScopeGuard.checkBranch(
                current.organizationId(),
                current.toBranchId()
        );

        return current;
    }

    @PostMapping("/{id}/approve")
    public StockTransferResponse approve(
            @PathVariable Long id,
            @RequestBody(required = false)
            ApproveStockTransferRequest request
    ) {
        StockTransferResponse current =
                service.findById(id);

        dataScopeGuard.checkBranch(
                current.organizationId(),
                current.fromBranchId()
        );

        dataScopeGuard.checkBranch(
                current.organizationId(),
                current.toBranchId()
        );

        return service.approve(
                id,
                request
        );
    }

    @PostMapping("/{id}/dispatch")
    public StockTransferResponse dispatch(
            @PathVariable Long id
    ) {
        StockTransferResponse current =
                service.findById(id);

        dataScopeGuard.checkBranch(
                current.organizationId(),
                current.fromBranchId()
        );

        dataScopeGuard.checkBranch(
                current.organizationId(),
                current.toBranchId()
        );

        return service.dispatch(id);
    }

    @PostMapping("/{id}/receive")
    public StockTransferResponse receive(
            @PathVariable Long id,
            @RequestBody(required = false)
            ReceiveStockTransferRequest request
    ) {
        StockTransferResponse current =
                service.findById(id);

        dataScopeGuard.checkBranch(
                current.organizationId(),
                current.fromBranchId()
        );

        dataScopeGuard.checkBranch(
                current.organizationId(),
                current.toBranchId()
        );

        return service.receive(
                id,
                request
        );
    }

    @PostMapping("/{id}/cancel")
    public StockTransferResponse cancel(
            @PathVariable Long id
    ) {
        StockTransferResponse current =
                service.findById(id);

        dataScopeGuard.checkBranch(
                current.organizationId(),
                current.fromBranchId()
        );

        dataScopeGuard.checkBranch(
                current.organizationId(),
                current.toBranchId()
        );

        return service.cancel(id);
    }
}
