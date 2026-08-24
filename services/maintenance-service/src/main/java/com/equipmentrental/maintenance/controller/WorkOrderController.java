package com.equipmentrental.maintenance.controller;

import com.equipmentrental.maintenance.dto.request.AssignWorkOrderRequest;
import com.equipmentrental.maintenance.dto.request.CancelWorkOrderRequest;
import com.equipmentrental.maintenance.dto.request.CompleteWorkOrderRequest;
import com.equipmentrental.maintenance.dto.request.CreateWorkOrderRequest;
import com.equipmentrental.maintenance.dto.request.UpdateWorkOrderRequest;
import com.equipmentrental.maintenance.dto.response.WorkOrderResponse;
import com.equipmentrental.maintenance.dto.response.WorkOrderTimelineResponse;
import com.equipmentrental.maintenance.enums.WorkOrderStatus;
import com.equipmentrental.maintenance.service.WorkOrderService;
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
import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/maintenance/work-orders"
)
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService service;

    @PostMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATIONS_STAFF')"
    )
    public ResponseEntity<WorkOrderResponse>
    create(
            @Valid
            @RequestBody
            CreateWorkOrderRequest request
    ) {

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        service.create(request)
                );
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN','MANAGER','OPERATIONS_STAFF')"
    )
    public Page<WorkOrderResponse>
    list(

            @RequestParam(required = false)
            WorkOrderStatus status,

            @RequestParam(required = false)
            Long branchId,

            @RequestParam(required = false)
            Long equipmentId,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime from,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime to,

            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
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

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMIN','MANAGER','OPERATIONS_STAFF')"
    )
    public WorkOrderResponse getById(
            @PathVariable Long id
    ) {

        return service.getById(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATIONS_STAFF')"
    )
    public WorkOrderResponse update(

            @PathVariable Long id,

            @Valid
            @RequestBody
            UpdateWorkOrderRequest request
    ) {

        return service.update(
                id,
                request
        );
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATIONS_STAFF')"
    )
    public WorkOrderResponse assign(

            @PathVariable Long id,

            @Valid
            @RequestBody
            AssignWorkOrderRequest request
    ) {

        return service.assign(
                id,
                request
        );
    }

    @PatchMapping("/{id}/start")
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATIONS_STAFF')"
    )
    public WorkOrderResponse start(
            @PathVariable Long id
    ) {

        return service.start(id);
    }

    @PatchMapping("/{id}/waiting-parts")
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATIONS_STAFF')"
    )
    public WorkOrderResponse waitingParts(
            @PathVariable Long id
    ) {

        return service.waitingParts(id);
    }

    @PatchMapping("/{id}/resume")
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATIONS_STAFF')"
    )
    public WorkOrderResponse resume(
            @PathVariable Long id
    ) {

        return service.resume(id);
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATIONS_STAFF')"
    )
    public WorkOrderResponse complete(

            @PathVariable Long id,

            @Valid
            @RequestBody
            CompleteWorkOrderRequest request
    ) {

        return service.complete(
                id,
                request
        );
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATIONS_STAFF')"
    )
    public WorkOrderResponse close(
            @PathVariable Long id
    ) {

        return service.close(id);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATIONS_STAFF')"
    )
    public WorkOrderResponse cancel(

            @PathVariable Long id,

            @Valid
            @RequestBody
            CancelWorkOrderRequest request
    ) {

        return service.cancel(
                id,
                request.reason()
        );
    }

    @GetMapping("/{id}/timeline")
    @PreAuthorize(
            "hasAnyRole('ADMIN','MANAGER','OPERATIONS_STAFF')"
    )
    public List<WorkOrderTimelineResponse>
    timeline(
            @PathVariable Long id
    ) {

        return service.timeline(id);
    }
}