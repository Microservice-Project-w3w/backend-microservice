package com.equipmentrental.maintenance.service;

import com.equipmentrental.maintenance.dto.request.CreatePartUsageRequest;
import com.equipmentrental.maintenance.dto.request.UpdatePartUsageRequest;
import com.equipmentrental.maintenance.dto.response.PartUsageResponse;
import com.equipmentrental.maintenance.entity.MaintenanceWorkOrder;
import com.equipmentrental.maintenance.entity.WorkOrderPartUsage;
import com.equipmentrental.maintenance.exception.ResourceNotFoundException;
import com.equipmentrental.maintenance.repository.MaintenanceWorkOrderRepository;
import com.equipmentrental.maintenance.repository.WorkOrderPartUsageRepository;
import com.equipmentrental.maintenance.security.CurrentUser;
import com.equipmentrental.maintenance.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PartUsageService {

    private final WorkOrderPartUsageRepository partUsageRepository;
    private final MaintenanceWorkOrderRepository workOrderRepository;
    private final CurrentUserService currentUserService;

    public PartUsageResponse create(
            Long workOrderId,
            CreatePartUsageRequest dto
    ) {

        MaintenanceWorkOrder workOrder =
                getAccessibleWorkOrder(workOrderId);

        CurrentUser current =
                currentUserService.getCurrentUser();

        WorkOrderPartUsage entity =
                WorkOrderPartUsage.builder()
                        .workOrderId(workOrder.getId())
                        .partId(dto.partId())
                        .partCodeSnapshot(dto.partCodeSnapshot())
                        .partNameSnapshot(dto.partNameSnapshot())
                        .quantity(dto.quantity())
                        .unit(dto.unit())
                        .unitCost(dto.unitCost())
                        .note(dto.note())
                        .createdByUserId(current.userId())
                        .build();

        entity = partUsageRepository.save(entity);

        partUsageRepository.flush();

        return toResponse(
                partUsageRepository
                        .findById(entity.getId())
                        .orElseThrow()
        );
    }

    @Transactional(readOnly = true)
    public List<PartUsageResponse> list(
            Long workOrderId
    ) {

        getAccessibleWorkOrder(workOrderId);

        return partUsageRepository
                .findByWorkOrderIdOrderByIdAsc(workOrderId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PartUsageResponse update(
            Long workOrderId,
            Long partUsageId,
            UpdatePartUsageRequest dto
    ) {

        getAccessibleWorkOrder(workOrderId);

        WorkOrderPartUsage entity =
                partUsageRepository
                        .findByIdAndWorkOrderId(
                                partUsageId,
                                workOrderId
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Không tìm thấy Part Usage id="
                                                + partUsageId
                                )
                        );

        if (dto.quantity() != null) {
            entity.setQuantity(dto.quantity());
        }

        if (dto.unit() != null) {
            entity.setUnit(dto.unit());
        }

        if (dto.unitCost() != null) {
            entity.setUnitCost(dto.unitCost());
        }

        if (dto.note() != null) {
            entity.setNote(dto.note());
        }

        partUsageRepository.save(entity);
        partUsageRepository.flush();

        return toResponse(
                partUsageRepository
                        .findById(entity.getId())
                        .orElseThrow()
        );
    }

    public void delete(
            Long workOrderId,
            Long partUsageId
    ) {

        getAccessibleWorkOrder(workOrderId);

        WorkOrderPartUsage entity =
                partUsageRepository
                        .findByIdAndWorkOrderId(
                                partUsageId,
                                workOrderId
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Không tìm thấy Part Usage id="
                                                + partUsageId
                                )
                        );

        partUsageRepository.delete(entity);
    }

    private MaintenanceWorkOrder getAccessibleWorkOrder(
            Long id
    ) {

        MaintenanceWorkOrder workOrder =
                workOrderRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Không tìm thấy Work Order id=" + id
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

    private PartUsageResponse toResponse(
            WorkOrderPartUsage entity
    ) {

        return new PartUsageResponse(
                entity.getId(),
                entity.getWorkOrderId(),
                entity.getPartId(),
                entity.getPartCodeSnapshot(),
                entity.getPartNameSnapshot(),
                entity.getQuantity(),
                entity.getUnit(),
                entity.getUnitCost(),
                entity.getTotalCost(),
                entity.getNote(),
                entity.getCreatedByUserId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}