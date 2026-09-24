package com.equipmentrental.maintenance.controller;

import com.equipmentrental.maintenance.dto.request.ApproveMaintenanceCostRequest;
import com.equipmentrental.maintenance.dto.request.CreateMaintenanceCostRequest;
import com.equipmentrental.maintenance.dto.request.UpdateMaintenanceCostRequest;
import com.equipmentrental.maintenance.dto.response.MaintenanceCostResponse;
import com.equipmentrental.maintenance.service.MaintenanceCostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maintenance")
@RequiredArgsConstructor
public class MaintenanceCostController {

    private final MaintenanceCostService service;

    @PostMapping("/work-orders/{workOrderId}/costs")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(
            "hasAuthority('maintenance.compensation.calculate')"
    )
    public MaintenanceCostResponse create(
            @PathVariable Long workOrderId,
            @Valid
            @RequestBody CreateMaintenanceCostRequest request
    ) {
        return service.create(workOrderId, request);
    }

    @GetMapping("/work-orders/{workOrderId}/costs")
    @PreAuthorize(
            "hasAuthority('maintenance.repair.read')"
    )
    public List<MaintenanceCostResponse> list(
            @PathVariable Long workOrderId
    ) {
        return service.list(workOrderId);
    }

    @PatchMapping("/costs/{costId}")
    @PreAuthorize(
            "hasAuthority('maintenance.compensation.calculate')"
    )
    public MaintenanceCostResponse update(
            @PathVariable Long costId,
            @Valid
            @RequestBody UpdateMaintenanceCostRequest request
    ) {
        return service.update(costId, request);
    }

    @PatchMapping("/costs/{costId}/approve")
    @PreAuthorize(
            "hasRole('ADMIN')"
    )
    public MaintenanceCostResponse approve(
            @PathVariable Long costId,
            @RequestBody(required = false)
            ApproveMaintenanceCostRequest request
    ) {

        if (request == null) {
            request =
                    new ApproveMaintenanceCostRequest(
                            null
                    );
        }

        return service.approve(costId, request);
    }
}
