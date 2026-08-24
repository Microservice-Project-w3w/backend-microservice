package com.equipmentrental.maintenance.controller;

import com.equipmentrental.maintenance.dto.response.CustomerIssueResponse;
import com.equipmentrental.maintenance.service.CustomerIssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/maintenance/issues"
)
@RequiredArgsConstructor
public class CustomerIssueOperationsController {

    private final CustomerIssueService service;

    @GetMapping
    @PreAuthorize(
            "hasAuthority('maintenance.incident.read')"
    )
    public List<CustomerIssueResponse> list() {

        return service.listForOperations();
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAuthority('maintenance.incident.read')"
    )
    public CustomerIssueResponse detail(
            @PathVariable Long id
    ) {

        return service.getForOperations(id);
    }

    @PatchMapping("/{id}/verify")
    @PreAuthorize(
            "hasAuthority('maintenance.incident.evaluate')"
    )
    public CustomerIssueResponse verify(
            @PathVariable Long id
    ) {

        return service.verify(id);
    }

    @PostMapping("/{id}/maintenance-request")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(
            "hasAuthority('maintenance.ticket.create')"
    )
    public CustomerIssueResponse createMaintenanceRequest(
            @PathVariable Long id
    ) {

        return service.createMaintenanceRequest(id);
    }
}
