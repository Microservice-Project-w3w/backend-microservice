package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.ConfirmStockOutRequest;
import com.equipmentrental.inventory.dto.request.CreateStockOutRequest;
import com.equipmentrental.inventory.dto.response.StockOutResponse;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
import com.equipmentrental.inventory.service.StockOutService;
import com.equipmentrental.inventory.service.WarehouseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/inventory/stock-out"
)
@RequiredArgsConstructor
public class StockOutController {

    private final StockOutService service;
private final InventoryDataScopeGuard dataScopeGuard;
private final WarehouseService warehouseService;
    @PostMapping
    @PreAuthorize("hasAuthority('inventory.stock.out')")
    public ResponseEntity<StockOutResponse> create(
            @Valid
            @RequestBody
            CreateStockOutRequest request
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
    @PreAuthorize("hasAuthority('inventory.stock.out')")
    public List<StockOutResponse> findAll(

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

        var warehouse =
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
        List<StockOutResponse> values = service.findAll(
                organizationId,
                branchId,
                warehouseId
        );
        return dataScopeGuard.filterAssignedBranches(
                organizationId,
                values,
                StockOutResponse::branchId
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory.stock.out')")
    public StockOutResponse findById(
            @PathVariable Long id
    ) {
 StockOutResponse current =
            service.findById(id);

    dataScopeGuard.checkBranch(
            current.organizationId(),
            current.branchId()
    );
        return current;
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('inventory.stock.out')")
    public StockOutResponse confirm(

            @PathVariable Long id,

            @RequestBody(required = false)
            ConfirmStockOutRequest request
    ) {
       StockOutResponse current =
            service.findById(id);

    dataScopeGuard.checkBranch(
            current.organizationId(),
            current.branchId()
    );
        return service.confirm(
                id,
                request
        );
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('inventory.stock.out')")
    public StockOutResponse cancel(
            @PathVariable Long id
    ) {
        StockOutResponse current =
            service.findById(id);

    dataScopeGuard.checkBranch(
            current.organizationId(),
            current.branchId()
    );

        return service.cancel(id);
    }
}
