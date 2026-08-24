package com.equipmentrental.maintenance.controller;

import com.equipmentrental.maintenance.dto.request.CreateAttachmentRequest;
import com.equipmentrental.maintenance.dto.response.AttachmentResponse;
import com.equipmentrental.maintenance.service.MaintenanceAttachmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maintenance")
@RequiredArgsConstructor
public class MaintenanceAttachmentController {

    private final MaintenanceAttachmentService service;

    @PostMapping(
            "/work-orders/{workOrderId}/attachments"
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATIONS_STAFF')"
    )
    public AttachmentResponse create(
            @PathVariable Long workOrderId,
            @Valid
            @RequestBody CreateAttachmentRequest request
    ) {
        return service.create(
                workOrderId,
                request
        );
    }

    @GetMapping(
            "/work-orders/{workOrderId}/attachments"
    )
    @PreAuthorize(
            "hasAnyRole('ADMIN','MANAGER','OPERATIONS_STAFF')"
    )
    public List<AttachmentResponse> list(
            @PathVariable Long workOrderId
    ) {
        return service.list(workOrderId);
    }

    @DeleteMapping(
            "/attachments/{attachmentId}"
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(
            "hasAnyRole('ADMIN','OPERATIONS_STAFF')"
    )
    public void delete(
            @PathVariable Long attachmentId
    ) {
        service.delete(attachmentId);
    }
}