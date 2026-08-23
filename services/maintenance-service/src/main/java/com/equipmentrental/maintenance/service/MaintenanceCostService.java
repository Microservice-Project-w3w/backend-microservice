package com.equipmentrental.maintenance.service;

import com.equipmentrental.maintenance.dto.request.ApproveMaintenanceCostRequest;
import com.equipmentrental.maintenance.dto.request.CreateMaintenanceCostRequest;
import com.equipmentrental.maintenance.dto.request.UpdateMaintenanceCostRequest;
import com.equipmentrental.maintenance.dto.response.MaintenanceCostResponse;
import com.equipmentrental.maintenance.entity.MaintenanceCost;
import com.equipmentrental.maintenance.entity.MaintenanceWorkOrder;
import com.equipmentrental.maintenance.enums.CostApprovalStatus;
import com.equipmentrental.maintenance.exception.BusinessException;
import com.equipmentrental.maintenance.exception.ResourceNotFoundException;
import com.equipmentrental.maintenance.repository.MaintenanceCostRepository;
import com.equipmentrental.maintenance.repository.MaintenanceWorkOrderRepository;
import com.equipmentrental.maintenance.security.CurrentUser;
import com.equipmentrental.maintenance.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceCostService {

    private final MaintenanceCostRepository costRepository;
    private final MaintenanceWorkOrderRepository workOrderRepository;
    private final CurrentUserService currentUserService;

    public MaintenanceCostResponse create(
            Long workOrderId,
            CreateMaintenanceCostRequest dto
    ) {

        MaintenanceWorkOrder workOrder =
                getAccessibleWorkOrder(workOrderId);

        CurrentUser current =
                currentUserService.getCurrentUser();

        MaintenanceCost entity =
                MaintenanceCost.builder()
                        .workOrderId(workOrder.getId())
                        .organizationId(workOrder.getOrganizationId())
                        .branchId(workOrder.getBranchId())
                        .costType(dto.costType())
                        .amount(dto.amount())
                        .currencyCode(
                                dto.currencyCode() == null
                                        ? "VND"
                                        : dto.currencyCode()
                                        .toUpperCase()
                        )
                        .description(dto.description())
                        .approvalStatus(
                                CostApprovalStatus.PENDING
                        )
                        .createdByUserId(current.userId())
                        .build();

        return toResponse(
                costRepository.save(entity)
        );
    }

    @Transactional(readOnly = true)
    public List<MaintenanceCostResponse> list(
            Long workOrderId
    ) {

        getAccessibleWorkOrder(workOrderId);

        return costRepository
                .findByWorkOrderIdOrderByCreatedAtAsc(
                        workOrderId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public MaintenanceCostResponse update(
            Long costId,
            UpdateMaintenanceCostRequest dto
    ) {

        MaintenanceCost entity =
                getAccessibleCost(costId);

        if (
                entity.getApprovalStatus()
                        == CostApprovalStatus.APPROVED
        ) {
            throw new BusinessException(
                    "Không thể sửa chi phí đã APPROVED"
            );
        }

        if (dto.costType() != null) {
            entity.setCostType(dto.costType());
        }

        if (dto.amount() != null) {
            entity.setAmount(dto.amount());
        }

        if (dto.currencyCode() != null) {
            entity.setCurrencyCode(
                    dto.currencyCode().toUpperCase()
            );
        }

        if (dto.description() != null) {
            entity.setDescription(dto.description());
        }

        entity.setUpdatedByUserId(
                currentUserService
                        .getCurrentUser()
                        .userId()
        );

        return toResponse(
                costRepository.save(entity)
        );
    }

    public MaintenanceCostResponse approve(
            Long costId,
            ApproveMaintenanceCostRequest dto
    ) {

        MaintenanceCost entity =
                getAccessibleCost(costId);

        if (
                entity.getApprovalStatus()
                        == CostApprovalStatus.APPROVED
        ) {
            throw new BusinessException(
                    "Chi phí đã được APPROVED"
            );
        }

        CurrentUser current =
                currentUserService.getCurrentUser();

        entity.setApprovalStatus(
                CostApprovalStatus.APPROVED
        );

        entity.setApprovedByUserId(
                current.userId()
        );

        entity.setApprovedAt(
                LocalDateTime.now()
        );

        entity.setApprovalNote(
                dto.approvalNote()
        );

        entity.setUpdatedByUserId(
                current.userId()
        );

        return toResponse(
                costRepository.save(entity)
        );
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

    private MaintenanceCost getAccessibleCost(
            Long id
    ) {

        MaintenanceCost cost =
                costRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Không tìm thấy Maintenance Cost id="
                                                + id
                                )
                        );

        currentUserService.requireOrganization(
                cost.getOrganizationId()
        );

        currentUserService.requireBranch(
                cost.getBranchId()
        );

        return cost;
    }

    private MaintenanceCostResponse toResponse(
            MaintenanceCost entity
    ) {

        return new MaintenanceCostResponse(
                entity.getId(),
                entity.getWorkOrderId(),
                entity.getOrganizationId(),
                entity.getBranchId(),
                entity.getCostType(),
                entity.getAmount(),
                entity.getCurrencyCode(),
                entity.getDescription(),
                entity.getApprovalStatus(),
                entity.getApprovedByUserId(),
                entity.getApprovedAt(),
                entity.getApprovalNote(),
                entity.getCreatedByUserId(),
                entity.getUpdatedByUserId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}