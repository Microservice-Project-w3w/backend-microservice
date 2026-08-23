package com.equipmentrental.maintenance.controller;

import com.equipmentrental.maintenance.dto.request.CreateInspectionRequest;
import com.equipmentrental.maintenance.dto.request.UpdateInspectionRequest;
import com.equipmentrental.maintenance.dto.response.InspectionResponse;
import com.equipmentrental.maintenance.service.InspectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maintenance")
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;

    @PostMapping(
            "/work-orders/{workOrderId}/inspections"
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATIONS_STAFF')"
    )
    public InspectionResponse create(
            @PathVariable Long workOrderId,
            @Valid
            @RequestBody CreateInspectionRequest request
    ) {

        return inspectionService.create(
                workOrderId,
                request
        );
    }

    @GetMapping(
            "/work-orders/{workOrderId}/inspections"
    )
    @PreAuthorize(
            "hasAnyRole('ADMIN','MANAGER','OPERATIONS_STAFF')"
    )
    public List<InspectionResponse> list(
            @PathVariable Long workOrderId
    ) {

        return inspectionService.list(
                workOrderId
        );
    }

    @GetMapping(
            "/inspections/{inspectionId}"
    )
    @PreAuthorize(
            "hasAnyRole('ADMIN','MANAGER','OPERATIONS_STAFF')"
    )
    public InspectionResponse getById(
            @PathVariable Long inspectionId
    ) {

        return inspectionService.getById(
                inspectionId
        );
    }

    @PatchMapping(
            "/inspections/{inspectionId}"
    )
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATIONS_STAFF')"
    )
    public InspectionResponse update(
            @PathVariable Long inspectionId,
            @Valid
            @RequestBody UpdateInspectionRequest request
    ) {

        return inspectionService.update(
                inspectionId,
                request
        );
    }

    @PatchMapping(
            "/inspections/{inspectionId}/submit"
    )
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATIONS_STAFF')"
    )
    public InspectionResponse submit(
            @PathVariable Long inspectionId
    ) {

        return inspectionService.submit(
                inspectionId
        );
    }
}