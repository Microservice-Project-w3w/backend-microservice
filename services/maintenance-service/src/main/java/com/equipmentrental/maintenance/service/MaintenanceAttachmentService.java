package com.equipmentrental.maintenance.service;

import com.equipmentrental.maintenance.dto.request.CreateAttachmentRequest;
import com.equipmentrental.maintenance.dto.response.AttachmentResponse;
import com.equipmentrental.maintenance.entity.MaintenanceAttachment;
import com.equipmentrental.maintenance.entity.MaintenanceWorkOrder;
import com.equipmentrental.maintenance.exception.ResourceNotFoundException;
import com.equipmentrental.maintenance.repository.MaintenanceAttachmentRepository;
import com.equipmentrental.maintenance.repository.MaintenanceWorkOrderRepository;
import com.equipmentrental.maintenance.security.CurrentUser;
import com.equipmentrental.maintenance.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceAttachmentService {

    private final MaintenanceAttachmentRepository attachmentRepository;
    private final MaintenanceWorkOrderRepository workOrderRepository;
    private final CurrentUserService currentUserService;

    public AttachmentResponse create(
            Long workOrderId,
            CreateAttachmentRequest dto
    ) {

        MaintenanceWorkOrder workOrder =
                getAccessibleWorkOrder(workOrderId);

        CurrentUser current =
                currentUserService.getCurrentUser();

        MaintenanceAttachment entity =
                MaintenanceAttachment.builder()
                        .workOrderId(workOrder.getId())
                        .organizationId(workOrder.getOrganizationId())
                        .branchId(workOrder.getBranchId())
                        .attachmentType(dto.attachmentType())
                        .fileName(dto.fileName())
                        .fileUrl(dto.fileUrl())
                        .mimeType(dto.mimeType())
                        .fileSizeBytes(dto.fileSizeBytes())
                        .description(dto.description())
                        .uploadedByUserId(current.userId())
                        .build();

        return toResponse(
                attachmentRepository.save(entity)
        );
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> list(
            Long workOrderId
    ) {

        getAccessibleWorkOrder(workOrderId);

        return attachmentRepository
                .findByWorkOrderIdOrderByUploadedAtAsc(workOrderId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void delete(
            Long attachmentId
    ) {

        MaintenanceAttachment entity =
                attachmentRepository
                        .findById(attachmentId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Không tìm thấy Attachment id="
                                                + attachmentId
                                )
                        );

        currentUserService.requireOrganization(
                entity.getOrganizationId()
        );

        currentUserService.requireBranch(
                entity.getBranchId()
        );

        attachmentRepository.delete(entity);
    }

    private MaintenanceWorkOrder getAccessibleWorkOrder(
            Long id
    ) {

        MaintenanceWorkOrder workOrder =
                workOrderRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Không tìm thấy Work Order id="
                                                + id
                                )
                        );

        currentUserService.requireOrganization(
                workOrder.getOrganizationId()
        );

        currentUserService.requireBranch(
                workOrder.getBranchId()
        );

        return workOrder;
    }

    private AttachmentResponse toResponse(
            MaintenanceAttachment entity
    ) {

        return new AttachmentResponse(
                entity.getId(),
                entity.getWorkOrderId(),
                entity.getOrganizationId(),
                entity.getBranchId(),
                entity.getAttachmentType(),
                entity.getFileName(),
                entity.getFileUrl(),
                entity.getMimeType(),
                entity.getFileSizeBytes(),
                entity.getDescription(),
                entity.getUploadedByUserId(),
                entity.getUploadedAt()
        );
    }
}