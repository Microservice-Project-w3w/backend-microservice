package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.ConfirmStockInRequest;
import com.equipmentrental.inventory.dto.request.CreateStockInRequest;
import com.equipmentrental.inventory.dto.response.StockInResponse;
import com.equipmentrental.inventory.dto.response.WarehouseResponse;
import com.equipmentrental.inventory.service.StockInService;
import com.equipmentrental.inventory.service.WarehouseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
@RestController
@PreAuthorize("hasAuthority('inventory.stock.in')")
@RequestMapping(
        "/api/v1/inventory/stock-in"
)
@RequiredArgsConstructor
public class StockInController {

    private final StockInService service;
    private final InventoryDataScopeGuard dataScopeGuard;
    private final WarehouseService warehouseService;
    @PostMapping
    public ResponseEntity<StockInResponse> create(
            @Valid
            @RequestBody
            CreateStockInRequest request
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
public List<StockInResponse> findAll(

        @RequestParam
        Long organizationId,

        @RequestParam(required = false)
        Long branchId,

        @RequestParam(required = false)
        Long warehouseId
) {

    // 1. Kiểm tra scope theo filter chính trước
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

    // 2. Nếu lọc theo warehouse thì resolve warehouse thật
    if (warehouseId != null) {

        WarehouseResponse warehouse =
                warehouseService.findById(
                        warehouseId
                );

        // User cũng phải có scope với warehouse thật
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

    List<StockInResponse> values = service.findAll(
            organizationId,
            branchId,
            warehouseId
    );
    return dataScopeGuard.filterAssignedBranches(
            organizationId,
            values,
            StockInResponse::branchId
    );
}

    @GetMapping("/{id}")
    public StockInResponse findById(
            @PathVariable Long id
    ) {
       StockInResponse current =
            service.findById(id);

    dataScopeGuard.checkBranch(
            current.organizationId(),
            current.branchId()
    );
        return current;
    }

    @PostMapping("/{id}/confirm")
    public StockInResponse confirm(

            @PathVariable Long id,

            @RequestBody(required = false)
            ConfirmStockInRequest request
    ) {
   StockInResponse current =
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
    public StockInResponse cancel(
            @PathVariable Long id
    ) {
           StockInResponse current =
            service.findById(id);

    dataScopeGuard.checkBranch(
            current.organizationId(),
            current.branchId()
    );
        return service.cancel(id);
    }
}
