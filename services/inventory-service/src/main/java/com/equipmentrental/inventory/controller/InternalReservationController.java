package com.equipmentrental.inventory.controller;

import com.equipmentrental.inventory.dto.request.ConfirmInternalReservationRequest;
import com.equipmentrental.inventory.dto.request.CreateInternalReservationRequest;
import com.equipmentrental.inventory.dto.request.ReleaseInternalReservationRequest;
import com.equipmentrental.inventory.dto.response.EquipmentReservationResponse;
import com.equipmentrental.inventory.dto.response.ReservationEquipmentOwnershipResponse;
import com.equipmentrental.inventory.security.InventoryDataScopeGuard;
import com.equipmentrental.inventory.service.EquipmentReservationQueryService;
import com.equipmentrental.inventory.service.InternalReservationService;
import com.equipmentrental.inventory.service.ReservationOwnershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/reservations")
@RequiredArgsConstructor
public class InternalReservationController {

    private final InternalReservationService service;

    private final EquipmentReservationQueryService queryService;

    private final InventoryDataScopeGuard dataScopeGuard;

    private final ReservationOwnershipService ownershipService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(
            "hasAuthority('inventory.reservation.create')"
    )
    public EquipmentReservationResponse create(
            @RequestBody
            CreateInternalReservationRequest request
    ) {

        dataScopeGuard.checkBranch(
                request.organizationId(),
                request.branchId()
        );

        return service.create(
                request
        );
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize(
            "hasAuthority('inventory.reservation.confirm')"
    )
    public EquipmentReservationResponse confirm(
            @PathVariable Long id,
            @RequestBody(required = false)
            ConfirmInternalReservationRequest request
    ) {

        EquipmentReservationResponse current =
                queryService.findById(id);

        dataScopeGuard.checkBranch(
                current.organizationId(),
                current.branchId()
        );

        return service.confirm(
                id,
                request
        );
    }

    @PostMapping("/{id}/release")
    @PreAuthorize(
            "hasAnyAuthority(" +
                    "'inventory.reservation.release'," +
                    "'rental.order.cancel'" +
                    ")"
    )
    public EquipmentReservationResponse release(
            @PathVariable Long id,
            @RequestBody
            ReleaseInternalReservationRequest request
    ) {

        EquipmentReservationResponse current =
                queryService.findById(id);

        dataScopeGuard.checkBranch(
                current.organizationId(),
                current.branchId()
        );

        return service.release(
                id,
                request
        );
    }

    @GetMapping(
            "/rental-order/{rentalOrderId}/equipment/{equipmentId}/ownership"
    )
    @PreAuthorize(
            "hasAnyAuthority(" +
                    "'inventory.reservation.read'," +
                    "'rental.order.read'" +
                    ") or hasRole('CUSTOMER')"
    )
    public ReservationEquipmentOwnershipResponse verifyOwnership(
            @PathVariable Long rentalOrderId,
            @PathVariable Long equipmentId
    ) {

        return ownershipService.verify(
                rentalOrderId,
                equipmentId
        );
    }
}