package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.response.EquipmentReservationResponse;
import com.equipmentrental.inventory.enums.ReservationStatus;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
import com.equipmentrental.inventory.service.EquipmentReservationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/reservations")
@RequiredArgsConstructor
public class EquipmentReservationController {

    private final EquipmentReservationQueryService service;
    private final InventoryDataScopeGuard dataScopeGuard;

    @GetMapping
    @PreAuthorize("hasAuthority('inventory.reservation.read')")
    public List<EquipmentReservationResponse> findAll(
            @RequestParam Long organizationId,
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long rentalOrderId,
            @RequestParam(required = false) ReservationStatus status
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

        List<EquipmentReservationResponse> values = service.findAll(
                organizationId,
                branchId,
                rentalOrderId,
                status
        );
        return dataScopeGuard.filterAssignedBranches(
                organizationId,
                values,
                EquipmentReservationResponse::branchId
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory.reservation.read')")
    public EquipmentReservationResponse findById(
            @PathVariable Long id
    ) {
        EquipmentReservationResponse current =
                service.findById(id);

        dataScopeGuard.checkBranch(
                current.organizationId(),
                current.branchId()
        );

        return current;
    }
}
