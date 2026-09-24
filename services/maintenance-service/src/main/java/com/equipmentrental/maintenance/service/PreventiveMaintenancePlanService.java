package com.equipmentrental.maintenance.service;

import com.equipmentrental.maintenance.dto.request.CreatePreventivePlanRequest;
import com.equipmentrental.maintenance.dto.request.UpdatePreventivePlanRequest;
import com.equipmentrental.maintenance.dto.response.PreventivePlanResponse;
import com.equipmentrental.maintenance.entity.MaintenanceWorkOrder;
import com.equipmentrental.maintenance.entity.PreventiveMaintenancePlan;
import com.equipmentrental.maintenance.entity.PreventivePlanRun;
import com.equipmentrental.maintenance.entity.WorkOrderTimeline;
import com.equipmentrental.maintenance.enums.Severity;
import com.equipmentrental.maintenance.enums.WorkOrderStatus;
import com.equipmentrental.maintenance.exception.BusinessException;
import com.equipmentrental.maintenance.exception.ResourceNotFoundException;
import com.equipmentrental.maintenance.repository.MaintenanceWorkOrderRepository;
import com.equipmentrental.maintenance.repository.PreventiveMaintenancePlanRepository;
import com.equipmentrental.maintenance.repository.PreventivePlanRunRepository;
import com.equipmentrental.maintenance.repository.WorkOrderTimelineRepository;
import com.equipmentrental.maintenance.security.CurrentUser;
import com.equipmentrental.maintenance.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PreventiveMaintenancePlanService {

    private final PreventiveMaintenancePlanRepository
            planRepository;

    private final PreventivePlanRunRepository
            planRunRepository;

    private final MaintenanceWorkOrderRepository
            workOrderRepository;

    private final WorkOrderTimelineRepository
            timelineRepository;

    private final CurrentUserService
            currentUserService;

    public PreventivePlanResponse create(
            CreatePreventivePlanRequest dto
    ) {

        CurrentUser current =
                currentUserService.getCurrentUser();

        if (dto.branchId() != null) {
            currentUserService.requireBranch(
                    dto.branchId()
            );
        }

        validateTarget(
                dto.equipmentId(),
                dto.equipmentTypeId()
        );

        PreventiveMaintenancePlan entity =
                PreventiveMaintenancePlan
                        .builder()
                        .planCode(
                                generatePlanCode()
                        )
                        .organizationId(
                                current.organizationId()
                        )
                        .branchId(
                                dto.branchId()
                        )
                        .equipmentId(
                                dto.equipmentId()
                        )
                        .equipmentTypeId(
                                dto.equipmentTypeId()
                        )
                        .name(
                                dto.name()
                        )
                        .description(
                                dto.description()
                        )
                        .frequencyType(
                                dto.frequencyType()
                        )
                        .frequencyValue(
                                dto.frequencyValue()
                        )
                        .nextMaintenanceDate(
                                dto.nextMaintenanceDate()
                        )
                        .active(true)
                        .createdByUserId(
                                current.userId()
                        )
                        .build();

        entity =
                planRepository.save(entity);

        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<PreventivePlanResponse> list() {

        CurrentUser current =
                currentUserService.getCurrentUser();

        return planRepository
                .findByOrganizationIdOrderByNextMaintenanceDateAsc(
                        current.organizationId()
                )
                .stream()
                .filter(
                        plan ->
                                current.hasRole("ADMIN")
                                        ||
                                        plan.getBranchId() == null
                                        ||
                                        current.branchIds()
                                                .contains(
                                                        plan.getBranchId()
                                                )
                )
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PreventivePlanResponse getById(
            Long id
    ) {

        return toResponse(
                getAccessiblePlan(id)
        );
    }

    public PreventivePlanResponse update(
            Long id,
            UpdatePreventivePlanRequest dto
    ) {

        PreventiveMaintenancePlan entity =
                getAccessiblePlan(id);

        CurrentUser current =
                currentUserService.getCurrentUser();

        if (dto.name() != null) {
            entity.setName(
                    dto.name()
            );
        }

        if (dto.description() != null) {
            entity.setDescription(
                    dto.description()
            );
        }

        if (dto.frequencyType() != null) {
            entity.setFrequencyType(
                    dto.frequencyType()
            );
        }

        if (dto.frequencyValue() != null) {

            if (dto.frequencyValue() <= 0) {
                throw new BusinessException(
                        "frequencyValue phải lớn hơn 0"
                );
            }

            entity.setFrequencyValue(
                    dto.frequencyValue()
            );
        }

        if (dto.nextMaintenanceDate() != null) {
            entity.setNextMaintenanceDate(
                    dto.nextMaintenanceDate()
            );
        }

        entity.setUpdatedByUserId(
                current.userId()
        );

        planRepository.save(entity);

        return toResponse(entity);
    }

    public PreventivePlanResponse activate(
            Long id
    ) {

        PreventiveMaintenancePlan entity =
                getAccessiblePlan(id);

        CurrentUser current =
                currentUserService.getCurrentUser();

        entity.setActive(true);

        entity.setUpdatedByUserId(
                current.userId()
        );

        planRepository.save(entity);

        return toResponse(entity);
    }

    public PreventivePlanResponse deactivate(
            Long id
    ) {

        PreventiveMaintenancePlan entity =
                getAccessiblePlan(id);

        CurrentUser current =
                currentUserService.getCurrentUser();

        entity.setActive(false);

        entity.setUpdatedByUserId(
                current.userId()
        );

        planRepository.save(entity);

        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<PreventivePlanResponse> due() {

        CurrentUser current =
                currentUserService.getCurrentUser();

        LocalDate today =
                LocalDate.now();

        return planRepository
                .findByOrganizationIdAndActiveTrueAndNextMaintenanceDateLessThanEqualOrderByNextMaintenanceDateAsc(
                        current.organizationId(),
                        today
                )
                .stream()
                .filter(
                        plan ->
                                current.hasRole("ADMIN")
                                        ||
                                        plan.getBranchId() == null
                                        ||
                                        current.branchIds()
                                                .contains(
                                                        plan.getBranchId()
                                                )
                )
                .map(this::toResponse)
                .toList();
    }

    public PreventivePlanResponse generateWorkOrder(
            Long id
    ) {

        PreventiveMaintenancePlan plan =
                getAccessiblePlan(id);

        if (!Boolean.TRUE.equals(plan.getActive())) {
            throw new BusinessException(
                    "Preventive Plan đang bị vô hiệu hóa"
            );
        }

        if (plan.getEquipmentId() == null) {
            throw new BusinessException(
                    "Hiện tại generate Work Order yêu cầu plan có equipmentId cụ thể"
            );
        }

        LocalDate dueDate =
                plan.getNextMaintenanceDate();

        if (
                planRunRepository
                        .existsByPlanIdAndDueDate(
                                plan.getId(),
                                dueDate
                        )
        ) {

            throw new BusinessException(
                    "Plan này đã sinh Work Order cho ngày "
                            + dueDate
            );
        }

        CurrentUser current =
                currentUserService.getCurrentUser();

        MaintenanceWorkOrder workOrder =
                MaintenanceWorkOrder
                        .builder()
                        .workOrderCode(
                                generateWorkOrderCode()
                        )
                        .requestId(null)
                        .organizationId(
                                plan.getOrganizationId()
                        )
                        .branchId(
                                plan.getBranchId()
                        )
                        .equipmentId(
                                plan.getEquipmentId()
                        )
                        .status(
                                WorkOrderStatus.OPEN
                        )
                        .priority(
                                Severity.MEDIUM
                        )
                        .title(
                                "Bảo trì định kỳ - "
                                        + plan.getName()
                        )
                        .description(
                                plan.getDescription()
                        )
                        .createdByUserId(
                                current.userId()
                        )
                        .build();

        workOrder =
                workOrderRepository.save(
                        workOrder
                );

        PreventivePlanRun run =
                PreventivePlanRun
                        .builder()
                        .planId(
                                plan.getId()
                        )
                        .workOrderId(
                                workOrder.getId()
                        )
                        .dueDate(
                                dueDate
                        )
                        .generatedByUserId(
                                current.userId()
                        )
                        .generatedByType(
                                "USER"
                        )
                        .build();

        planRunRepository.save(run);

        WorkOrderTimeline timeline =
                WorkOrderTimeline
                        .builder()
                        .workOrderId(
                                workOrder.getId()
                        )
                        .eventType(
                                "CREATED"
                        )
                        .fromStatus(null)
                        .toStatus(
                                WorkOrderStatus.OPEN.name()
                        )
                        .actorUserId(
                                current.userId()
                        )
                        .actorType(
                                "USER"
                        )
                        .message(
                                "Sinh Work Order từ Preventive Plan id="
                                        + plan.getId()
                        )
                        .build();

        timelineRepository.save(
                timeline
        );

        plan.setLastMaintenanceDate(
                dueDate
        );

        plan.setNextMaintenanceDate(
                calculateNextDate(
                        dueDate,
                        plan
                )
        );

        plan.setUpdatedByUserId(
                current.userId()
        );

        planRepository.save(plan);

        return toResponse(plan);
    }

    private PreventiveMaintenancePlan
    getAccessiblePlan(
            Long id
    ) {

        PreventiveMaintenancePlan plan =
                planRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Không tìm thấy Preventive Plan id="
                                                        + id
                                        )
                        );

        currentUserService
                .requireOrganization(
                        plan.getOrganizationId()
                );

        if (plan.getBranchId() != null) {
            currentUserService
                    .requireBranch(
                            plan.getBranchId()
                    );
        }

        return plan;
    }

    private void validateTarget(
            Long equipmentId,
            Long equipmentTypeId
    ) {

        boolean equipmentProvided =
                equipmentId != null;

        boolean equipmentTypeProvided =
                equipmentTypeId != null;

        if (
                equipmentProvided
                        == equipmentTypeProvided
        ) {

            throw new BusinessException(
                    "Phải chọn đúng một trong equipmentId hoặc equipmentTypeId"
            );
        }
    }

    private LocalDate calculateNextDate(
            LocalDate currentDate,
            PreventiveMaintenancePlan plan
    ) {

        return switch (
                plan.getFrequencyType()
                ) {

            case DAY ->
                    currentDate.plusDays(
                            plan.getFrequencyValue()
                    );

            case WEEK ->
                    currentDate.plusWeeks(
                            plan.getFrequencyValue()
                    );

            case MONTH ->
                    currentDate.plusMonths(
                            plan.getFrequencyValue()
                    );

            case YEAR ->
                    currentDate.plusYears(
                            plan.getFrequencyValue()
                    );

            case USAGE_HOUR,
                 USAGE_CYCLE ->
                    throw new BusinessException(
                            "USAGE_HOUR và USAGE_CYCLE cần dữ liệu usage từ Inventory/Telemetry"
                    );
        };
    }

    private PreventivePlanResponse toResponse(
            PreventiveMaintenancePlan entity
    ) {

        return new PreventivePlanResponse(
                entity.getId(),
                entity.getPlanCode(),
                entity.getOrganizationId(),
                entity.getBranchId(),
                entity.getEquipmentId(),
                entity.getEquipmentTypeId(),
                entity.getName(),
                entity.getDescription(),
                entity.getFrequencyType(),
                entity.getFrequencyValue(),
                entity.getLastMaintenanceDate(),
                entity.getNextMaintenanceDate(),
                entity.getActive(),
                entity.getCreatedByUserId(),
                entity.getUpdatedByUserId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String generatePlanCode() {

        return "PM-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10)
                .toUpperCase();
    }

    private String generateWorkOrderCode() {

        return "WO-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10)
                .toUpperCase();
    }
}