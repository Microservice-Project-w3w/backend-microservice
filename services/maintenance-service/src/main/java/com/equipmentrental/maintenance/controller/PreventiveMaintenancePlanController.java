package com.equipmentrental.maintenance.controller;

import com.equipmentrental.maintenance.dto.request.CreatePreventivePlanRequest;
import com.equipmentrental.maintenance.dto.request.UpdatePreventivePlanRequest;
import com.equipmentrental.maintenance.dto.response.PreventivePlanResponse;
import com.equipmentrental.maintenance.service.PreventiveMaintenancePlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maintenance/plans")
@RequiredArgsConstructor
public class PreventiveMaintenancePlanController {

    private final PreventiveMaintenancePlanService
            service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATIONS_STAFF')"
    )
    public PreventivePlanResponse create(
            @Valid
            @RequestBody
            CreatePreventivePlanRequest request
    ) {

        return service.create(
                request
        );
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN','MANAGER','OPERATIONS_STAFF')"
    )
    public List<PreventivePlanResponse> list() {

        return service.list();
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMIN','MANAGER','OPERATIONS_STAFF')"
    )
    public PreventivePlanResponse getById(
            @PathVariable Long id
    ) {

        return service.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATIONS_STAFF')"
    )
    public PreventivePlanResponse update(
            @PathVariable Long id,
            @Valid
            @RequestBody
            UpdatePreventivePlanRequest request
    ) {

        return service.update(
                id,
                request
        );
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATIONS_STAFF')"
    )
    public PreventivePlanResponse activate(
            @PathVariable Long id
    ) {

        return service.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATIONS_STAFF')"
    )
    public PreventivePlanResponse deactivate(
            @PathVariable Long id
    ) {

        return service.deactivate(id);
    }

    @GetMapping("/due")
    @PreAuthorize(
            "hasAnyRole('ADMIN','MANAGER','OPERATIONS_STAFF')"
    )
    public List<PreventivePlanResponse> due() {

        return service.due();
    }

    @PostMapping("/{id}/generate-work-order")
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATIONS_STAFF')"
    )
    public PreventivePlanResponse generateWorkOrder(
            @PathVariable Long id
    ) {

        return service.generateWorkOrder(id);
    }
}