package com.equipmentrental.maintenance.controller;

import com.equipmentrental.maintenance.dto.request.CancelMaintenanceRequestRequest;
import com.equipmentrental.maintenance.dto.request.CreateMaintenanceRequestRequest;
import com.equipmentrental.maintenance.dto.request.UpdateMaintenanceRequestRequest;
import com.equipmentrental.maintenance.dto.response.MaintenanceRequestResponse;
import com.equipmentrental.maintenance.dto.response.WorkOrderSummaryResponse;
import com.equipmentrental.maintenance.enums.MaintenanceRequestStatus;
import com.equipmentrental.maintenance.service.MaintenanceRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping(
        "/api/v1/maintenance/requests"
)
@RequiredArgsConstructor
public class MaintenanceRequestController {

    private final MaintenanceRequestService
            service;

    /*
     * POST /requests
     *
     * ADMIN
     * OPERATIONS_STAFF
     */
    @PostMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATIONS_STAFF')"
    )
    public ResponseEntity<MaintenanceRequestResponse>
    create(

            @Valid
            @RequestBody
            CreateMaintenanceRequestRequest request
    ) {

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        service.create(request)
                );
    }

    /*
     * GET /requests
     *
     * ADMIN
     * MANAGER
     * OPERATIONS_STAFF
     */
    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN','MANAGER','OPERATIONS_STAFF')"
    )
    public Page<MaintenanceRequestResponse>
    list(

            @RequestParam(required = false)
            MaintenanceRequestStatus status,

            @RequestParam(required = false)
            Long branchId,

            @RequestParam(required = false)
            Long equipmentId,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso =
                            DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime from,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso =
                            DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime to,

            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "createdAt",
                    direction =
                            Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        return service.list(
                status,
                branchId,
                equipmentId,
                from,
                to,
                pageable
        );
    }

    /*
     * GET /requests/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMIN','MANAGER','OPERATIONS_STAFF')"
    )
    public MaintenanceRequestResponse getById(
            @PathVariable Long id
    ) {

        return service.getById(id);
    }

    /*
     * PATCH /requests/{id}
     */
    @PatchMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATIONS_STAFF')"
    )
    public MaintenanceRequestResponse update(

            @PathVariable
            Long id,

            @Valid
            @RequestBody
            UpdateMaintenanceRequestRequest request
    ) {

        return service.update(
                id,
                request
        );
    }

    /*
     * PATCH /requests/{id}/cancel
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATIONS_STAFF')"
    )
    public MaintenanceRequestResponse cancel(

            @PathVariable
            Long id,

            @Valid
            @RequestBody
            CancelMaintenanceRequestRequest request
    ) {

        return service.cancel(
                id,
                request.reason()
        );
    }

    /*
     * POST /requests/{id}/work-order
     */
    @PostMapping("/{id}/work-order")
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATIONS_STAFF')"
    )
    public ResponseEntity<WorkOrderSummaryResponse>
    createWorkOrder(

            @PathVariable
            Long id
    ) {

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        service.createWorkOrder(id)
                );
    }
}