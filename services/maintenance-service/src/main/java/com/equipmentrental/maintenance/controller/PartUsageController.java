package com.equipmentrental.maintenance.controller;

import com.equipmentrental.maintenance.dto.request.CreatePartUsageRequest;
import com.equipmentrental.maintenance.dto.request.UpdatePartUsageRequest;
import com.equipmentrental.maintenance.dto.response.PartUsageResponse;
import com.equipmentrental.maintenance.service.PartUsageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maintenance/work-orders/{workOrderId}/parts")
@RequiredArgsConstructor
public class PartUsageController {

    private final PartUsageService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('maintenance.part.manage')")
    public PartUsageResponse create(
            @PathVariable Long workOrderId,
            @Valid @RequestBody CreatePartUsageRequest request
    ) {
        return service.create(workOrderId, request);
    }

    @GetMapping
    @PreAuthorize(
            "hasAuthority('maintenance.part.read')"
    )
    public List<PartUsageResponse> list(
            @PathVariable Long workOrderId
    ) {
        return service.list(workOrderId);
    }

    @PatchMapping("/{partUsageId}")
    @PreAuthorize("hasAuthority('maintenance.part.manage')")
    public PartUsageResponse update(
            @PathVariable Long workOrderId,
            @PathVariable Long partUsageId,
            @Valid @RequestBody UpdatePartUsageRequest request
    ) {
        return service.update(
                workOrderId,
                partUsageId,
                request
        );
    }

    @DeleteMapping("/{partUsageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('maintenance.part.manage')")
    public void delete(
            @PathVariable Long workOrderId,
            @PathVariable Long partUsageId
    ) {
        service.delete(workOrderId, partUsageId);
    }
}
