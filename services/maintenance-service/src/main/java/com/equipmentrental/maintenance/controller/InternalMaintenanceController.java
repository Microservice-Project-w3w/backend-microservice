package com.equipmentrental.maintenance.controller;

import com.equipmentrental.maintenance.dto.request.InternalCreateMaintenanceRequest;
import com.equipmentrental.maintenance.dto.response.EquipmentMaintenanceStateResponse;
import com.equipmentrental.maintenance.dto.response.EquipmentRentalBlockResponse;
import com.equipmentrental.maintenance.dto.response.MaintenanceRequestResponse;
import com.equipmentrental.maintenance.service.InternalMaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/maintenance")
@RequiredArgsConstructor
public class InternalMaintenanceController {

    private final InternalMaintenanceService service;

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(
            "hasAuthority('maintenance.ticket.create')"
    )
    public MaintenanceRequestResponse createRequest(
            @Valid
            @RequestBody
            InternalCreateMaintenanceRequest request
    ) {

        return service.createFromLogistics(request);
    }

    @GetMapping("/equipment/{id}/state")
    @PreAuthorize(
            "hasAnyAuthority('maintenance.history.read','inventory.availability.read')"
    )
    public EquipmentMaintenanceStateResponse getState(
            @PathVariable Long id
    ) {

        return service.getState(id);
    }

    @GetMapping("/equipment/{id}/rental-block")
    @PreAuthorize(
            "hasAnyAuthority('maintenance.history.read','inventory.availability.read')"
    )
    public EquipmentRentalBlockResponse getRentalBlock(
            @PathVariable Long id
    ) {

        return service.getRentalBlock(id);
    }
}
