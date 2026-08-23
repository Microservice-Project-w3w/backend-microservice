package com.equipmentrental.maintenance.service;

import com.equipmentrental.maintenance.dto.request.CreateInspectionRequest;
import com.equipmentrental.maintenance.dto.request.InspectionChecklistItemRequest;
import com.equipmentrental.maintenance.dto.request.UpdateInspectionRequest;
import com.equipmentrental.maintenance.dto.response.InspectionChecklistItemResponse;
import com.equipmentrental.maintenance.dto.response.InspectionResponse;
import com.equipmentrental.maintenance.entity.InspectionChecklistItem;
import com.equipmentrental.maintenance.entity.MaintenanceInspection;
import com.equipmentrental.maintenance.entity.MaintenanceWorkOrder;
import com.equipmentrental.maintenance.entity.WorkOrderTimeline;
import com.equipmentrental.maintenance.enums.InspectionItemResult;
import com.equipmentrental.maintenance.enums.InspectionStatus;
import com.equipmentrental.maintenance.exception.BusinessException;
import com.equipmentrental.maintenance.exception.ResourceNotFoundException;
import com.equipmentrental.maintenance.repository.InspectionChecklistItemRepository;
import com.equipmentrental.maintenance.repository.MaintenanceInspectionRepository;
import com.equipmentrental.maintenance.repository.MaintenanceWorkOrderRepository;
import com.equipmentrental.maintenance.repository.WorkOrderTimelineRepository;
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
public class InspectionService {

    private final MaintenanceInspectionRepository
            inspectionRepository;

    private final InspectionChecklistItemRepository
            checklistRepository;

    private final MaintenanceWorkOrderRepository
            workOrderRepository;

    private final WorkOrderTimelineRepository
            timelineRepository;

    private final CurrentUserService
            currentUserService;

    public InspectionResponse create(
            Long workOrderId,
            CreateInspectionRequest dto
    ) {

        MaintenanceWorkOrder workOrder =
                getAccessibleWorkOrder(workOrderId);

        CurrentUser current =
                currentUserService.getCurrentUser();

        MaintenanceInspection inspection =
                MaintenanceInspection.builder()
                        .workOrderId(workOrder.getId())
                        .organizationId(workOrder.getOrganizationId())
                        .branchId(workOrder.getBranchId())
                        .equipmentId(workOrder.getEquipmentId())
                        .inspectionCondition(dto.condition())
                        .severity(dto.severity())
                        .result(dto.result())
                        .cause(dto.cause())
                        .recommendation(dto.recommendation())
                        .notes(dto.notes())
                        .status(InspectionStatus.DRAFT)
                        .inspectedByUserId(current.userId())
                        .inspectedAt(LocalDateTime.now())
                        .build();

        inspection =
                inspectionRepository.save(inspection);

        saveChecklist(
                inspection.getId(),
                dto.checklist()
        );

        return toResponse(inspection);
    }

    @Transactional(readOnly = true)
    public List<InspectionResponse> list(
            Long workOrderId
    ) {

        getAccessibleWorkOrder(workOrderId);

        return inspectionRepository
                .findByWorkOrderIdOrderByInspectedAtDesc(
                        workOrderId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InspectionResponse getById(
            Long inspectionId
    ) {

        MaintenanceInspection inspection =
                getAccessibleInspection(
                        inspectionId
                );

        return toResponse(inspection);
    }

    public InspectionResponse update(
            Long inspectionId,
            UpdateInspectionRequest dto
    ) {

        MaintenanceInspection inspection =
                getAccessibleInspection(
                        inspectionId
                );

        if (
                inspection.getStatus()
                        == InspectionStatus.SUBMITTED
        ) {

            throw new BusinessException(
                    "Không thể cập nhật Inspection đã SUBMITTED"
            );
        }

        if (dto.condition() != null) {
            inspection.setInspectionCondition(
                    dto.condition()
            );
        }

        if (dto.severity() != null) {
            inspection.setSeverity(
                    dto.severity()
            );
        }

        if (dto.result() != null) {
            inspection.setResult(
                    dto.result()
            );
        }

        if (dto.cause() != null) {
            inspection.setCause(
                    dto.cause()
            );
        }

        if (dto.recommendation() != null) {
            inspection.setRecommendation(
                    dto.recommendation()
            );
        }

        if (dto.notes() != null) {
            inspection.setNotes(
                    dto.notes()
            );
        }

        if (dto.checklist() != null) {

            checklistRepository
                    .deleteByInspectionId(
                            inspection.getId()
                    );

            saveChecklist(
                    inspection.getId(),
                    dto.checklist()
            );
        }

        inspectionRepository.save(inspection);

        return toResponse(inspection);
    }

    public InspectionResponse submit(
            Long inspectionId
    ) {

        MaintenanceInspection inspection =
                getAccessibleInspection(
                        inspectionId
                );

        if (
                inspection.getStatus()
                        == InspectionStatus.SUBMITTED
        ) {

            throw new BusinessException(
                    "Inspection đã được submit"
            );
        }

        CurrentUser current =
                currentUserService.getCurrentUser();

        inspection.setStatus(
                InspectionStatus.SUBMITTED
        );

        inspection.setSubmittedByUserId(
                current.userId()
        );

        inspection.setSubmittedAt(
                LocalDateTime.now()
        );

        inspectionRepository.save(inspection);

        addTimeline(
                inspection,
                current.userId()
        );

        return toResponse(inspection);
    }

    private MaintenanceWorkOrder
    getAccessibleWorkOrder(
            Long workOrderId
    ) {

        MaintenanceWorkOrder workOrder =
                workOrderRepository
                        .findById(workOrderId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Không tìm thấy Work Order id="
                                                        + workOrderId
                                        )
                        );

        currentUserService
                .requireOrganization(
                        workOrder.getOrganizationId()
                );

        currentUserService
                .requireBranch(
                        workOrder.getBranchId()
                );

        return workOrder;
    }

    private MaintenanceInspection
    getAccessibleInspection(
            Long inspectionId
    ) {

        MaintenanceInspection inspection =
                inspectionRepository
                        .findById(inspectionId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Không tìm thấy Inspection id="
                                                        + inspectionId
                                        )
                        );

        currentUserService
                .requireOrganization(
                        inspection.getOrganizationId()
                );

        currentUserService
                .requireBranch(
                        inspection.getBranchId()
                );

        return inspection;
    }

    private void saveChecklist(
            Long inspectionId,
            List<InspectionChecklistItemRequest> checklist
    ) {

        if (checklist == null) {
            return;
        }

        int defaultOrder = 0;

        for (
                InspectionChecklistItemRequest dto
                : checklist
        ) {

            InspectionChecklistItem item =
                    InspectionChecklistItem
                            .builder()
                            .inspectionId(inspectionId)
                            .itemCode(dto.itemCode())
                            .itemName(dto.itemName())
                            .expectedValue(
                                    dto.expectedValue()
                            )
                            .actualValue(
                                    dto.actualValue()
                            )
                            .itemResult(
                                    dto.itemResult() == null
                                            ? InspectionItemResult.NOT_CHECKED
                                            : dto.itemResult()
                            )
                            .note(dto.note())
                            .sortOrder(
                                    dto.sortOrder() == null
                                            ? defaultOrder
                                            : dto.sortOrder()
                            )
                            .build();

            checklistRepository.save(item);

            defaultOrder++;
        }
    }

    private InspectionResponse toResponse(
            MaintenanceInspection inspection
    ) {

        List<InspectionChecklistItemResponse>
                checklist =

                checklistRepository
                        .findByInspectionIdOrderBySortOrderAsc(
                                inspection.getId()
                        )
                        .stream()
                        .map(
                                item ->
                                        new InspectionChecklistItemResponse(
                                                item.getId(),
                                                item.getItemCode(),
                                                item.getItemName(),
                                                item.getExpectedValue(),
                                                item.getActualValue(),
                                                item.getItemResult(),
                                                item.getNote(),
                                                item.getSortOrder()
                                        )
                        )
                        .toList();

        return new InspectionResponse(
                inspection.getId(),
                inspection.getWorkOrderId(),
                inspection.getOrganizationId(),
                inspection.getBranchId(),
                inspection.getEquipmentId(),
                inspection.getInspectionCondition(),
                inspection.getSeverity(),
                inspection.getResult(),
                inspection.getCause(),
                inspection.getRecommendation(),
                inspection.getNotes(),
                inspection.getStatus(),
                inspection.getInspectedByUserId(),
                inspection.getInspectedAt(),
                inspection.getSubmittedByUserId(),
                inspection.getSubmittedAt(),
                inspection.getCreatedAt(),
                inspection.getUpdatedAt(),
                checklist
        );
    }

    private void addTimeline(
            MaintenanceInspection inspection,
            Long actorUserId
    ) {

        MaintenanceWorkOrder workOrder =
                workOrderRepository
                        .findById(
                                inspection.getWorkOrderId()
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Không tìm thấy Work Order id="
                                                        + inspection.getWorkOrderId()
                                        )
                        );

        WorkOrderTimeline timeline =
                WorkOrderTimeline
                        .builder()
                        .workOrderId(
                                workOrder.getId()
                        )
                        .eventType(
                                "INSPECTION_SUBMITTED"
                        )
                        .fromStatus(
                                workOrder.getStatus().name()
                        )
                        .toStatus(
                                workOrder.getStatus().name()
                        )
                        .actorUserId(
                                actorUserId
                        )
                        .actorType(
                                "USER"
                        )
                        .message(
                                "Submit kết quả kiểm tra thiết bị"
                        )
                        .build();

        timelineRepository.save(timeline);
    }
}