package com.equipmentrental.maintenance.service;

import com.equipmentrental.maintenance.client.InventoryClient;
import com.equipmentrental.maintenance.dto.request.AssignWorkOrderRequest;
import com.equipmentrental.maintenance.dto.request.CompleteWorkOrderRequest;
import com.equipmentrental.maintenance.dto.request.CreateWorkOrderRequest;
import com.equipmentrental.maintenance.dto.request.UpdateWorkOrderRequest;
import com.equipmentrental.maintenance.dto.response.WorkOrderResponse;
import com.equipmentrental.maintenance.dto.response.WorkOrderTimelineResponse;
import com.equipmentrental.maintenance.entity.MaintenanceWorkOrder;
import com.equipmentrental.maintenance.entity.WorkOrderTimeline;
import com.equipmentrental.maintenance.enums.WorkOrderResult;
import com.equipmentrental.maintenance.enums.WorkOrderStatus;
import com.equipmentrental.maintenance.exception.BusinessException;
import com.equipmentrental.maintenance.exception.ResourceNotFoundException;
import com.equipmentrental.maintenance.mapper.WorkOrderMapper;
import com.equipmentrental.maintenance.repository.MaintenanceWorkOrderRepository;
import com.equipmentrental.maintenance.repository.WorkOrderTimelineRepository;
import com.equipmentrental.maintenance.repository.spec.WorkOrderSpecifications;
import com.equipmentrental.maintenance.security.CurrentUser;
import com.equipmentrental.maintenance.security.CurrentUserService;
import com.equipmentrental.maintenance.enums.CustomerIssueStatus;
import com.equipmentrental.maintenance.repository.CustomerIssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkOrderService {

    private final MaintenanceWorkOrderRepository
            workOrderRepository;

    private final WorkOrderTimelineRepository
            timelineRepository;

    private final CustomerIssueRepository
            customerIssueRepository;

    private final CurrentUserService
            currentUserService;

    private final WorkOrderMapper
            mapper;

    private final InventoryClient
            inventoryClient;
    ;

    public WorkOrderResponse create(
            CreateWorkOrderRequest dto
    ) {

        CurrentUser current =
                currentUserService
                        .getCurrentUser();

        currentUserService
                .requireBranch(
                        dto.branchId()
                );

        if (
                dto.expectedStartAt() != null
                        &&
                        dto.expectedCompleteAt() != null
                        &&
                        dto.expectedCompleteAt()
                                .isBefore(
                                        dto.expectedStartAt()
                                )
        ) {

            throw new BusinessException(
                    "expectedCompleteAt không được trước expectedStartAt"
            );
        }

        if (
                dto.requestId() != null
                        &&
                        workOrderRepository
                                .existsByRequestId(
                                        dto.requestId()
                                )
        ) {

            throw new BusinessException(
                    "Maintenance Request này đã có Work Order"
            );
        }

        MaintenanceWorkOrder entity =
                MaintenanceWorkOrder
                        .builder()

                        .workOrderCode(
                                generateCode()
                        )

                        .requestId(
                                dto.requestId()
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

                        .priority(
                                dto.priority()
                        )

                        .title(
                                dto.title()
                        )

                        .description(
                                dto.description()
                        )

                        .expectedStartAt(
                                dto.expectedStartAt()
                        )

                        .expectedCompleteAt(
                                dto.expectedCompleteAt()
                        )

                        .status(
                                WorkOrderStatus.OPEN
                        )

                        .createdByUserId(
                                current.userId()
                        )

                        .build();

        entity =
                workOrderRepository
                        .save(entity);

        addTimeline(
                entity,
                "CREATED",
                null,
                WorkOrderStatus.OPEN,
                "Tạo Work Order"
        );

        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public Page<WorkOrderResponse> list(

            WorkOrderStatus status,

            Long branchId,

            Long equipmentId,

            LocalDateTime from,

            LocalDateTime to,

            Pageable pageable
    ) {

        CurrentUser current =
                currentUserService
                        .getCurrentUser();

        if (branchId != null) {
            currentUserService
                    .requireBranch(branchId);
        }

        Specification<MaintenanceWorkOrder>
                specification =

                Specification
                        .where(
                                WorkOrderSpecifications
                                        .organizationEquals(
                                                current.organizationId()
                                        )
                        )

                        .and(
                                WorkOrderSpecifications
                                        .statusEquals(
                                                status
                                        )
                        )

                        .and(
                                WorkOrderSpecifications
                                        .branchEquals(
                                                branchId
                                        )
                        )

                        .and(
                                WorkOrderSpecifications
                                        .equipmentEquals(
                                                equipmentId
                                        )
                        )

                        .and(
                                WorkOrderSpecifications
                                        .createdFrom(
                                                from
                                        )
                        )

                        .and(
                                WorkOrderSpecifications
                                        .createdTo(
                                                to
                                        )
                        );

        if (
                !current.hasRole("ADMIN")
                        &&
                        branchId == null
        ) {

            specification =
                    specification.and(
                            WorkOrderSpecifications
                                    .branchIn(
                                            current.branchIds()
                                    )
                    );
        }

        return workOrderRepository
                .findAll(
                        specification,
                        pageable
                )
                .map(
                        mapper::toResponse
                );
    }

    @Transactional(readOnly = true)
    public WorkOrderResponse getById(
            Long id
    ) {

        return mapper.toResponse(
                getAccessibleWorkOrder(id)
        );
    }

    public WorkOrderResponse update(
            Long id,
            UpdateWorkOrderRequest dto
    ) {

        MaintenanceWorkOrder entity =
                getAccessibleWorkOrder(id);

        if (
                entity.getStatus()
                        == WorkOrderStatus.CLOSED
                        ||
                        entity.getStatus()
                                == WorkOrderStatus.CANCELLED
        ) {

            throw new BusinessException(
                    "Không thể cập nhật Work Order đã đóng hoặc hủy"
            );
        }

        if (dto.priority() != null) {
            entity.setPriority(
                    dto.priority()
            );
        }

        if (dto.title() != null) {
            entity.setTitle(
                    dto.title()
            );
        }

        if (dto.description() != null) {
            entity.setDescription(
                    dto.description()
            );
        }

        if (dto.notes() != null) {
            entity.setNotes(
                    dto.notes()
            );
        }

        if (dto.expectedStartAt() != null) {
            entity.setExpectedStartAt(
                    dto.expectedStartAt()
            );
        }

        if (dto.expectedCompleteAt() != null) {
            entity.setExpectedCompleteAt(
                    dto.expectedCompleteAt()
            );
        }

        if (
                entity.getExpectedStartAt() != null
                        &&
                        entity.getExpectedCompleteAt() != null
                        &&
                        entity.getExpectedCompleteAt()
                                .isBefore(
                                        entity.getExpectedStartAt()
                                )
        ) {

            throw new BusinessException(
                    "expectedCompleteAt không được trước expectedStartAt"
            );
        }

        addTimeline(
                entity,
                "UPDATED",
                entity.getStatus(),
                entity.getStatus(),
                "Cập nhật thông tin Work Order"
        );

        return mapper.toResponse(entity);
    }

    public WorkOrderResponse assign(
            Long id,
            AssignWorkOrderRequest dto
    ) {

        MaintenanceWorkOrder entity =
                getAccessibleWorkOrder(id);

        requireStatus(
                entity,
                WorkOrderStatus.OPEN
        );

        WorkOrderStatus oldStatus =
                entity.getStatus();

        entity.setAssignedUserId(
                dto.assignedUserId()
        );

        entity.setStatus(
                WorkOrderStatus.ASSIGNED
        );

        addTimeline(
                entity,
                "ASSIGNED",
                oldStatus,
                entity.getStatus(),
                "Phân công người xử lý: "
                        + dto.assignedUserId()
        );

        return mapper.toResponse(entity);
    }

    public WorkOrderResponse start(
            Long id
    ) {

        MaintenanceWorkOrder entity =
                getAccessibleWorkOrder(id);

        requireStatus(
                entity,
                WorkOrderStatus.ASSIGNED
        );

        WorkOrderStatus oldStatus =
                entity.getStatus();

        entity.setStatus(
                WorkOrderStatus.IN_PROGRESS
        );

        entity.setActualStartAt(
                LocalDateTime.now()
        );

        /*
         * Không sửa inventory_db trực tiếp.
         */
        inventoryClient
                .changeEquipmentStatus(
                        entity.getEquipmentId(),
                        "MAINTENANCE"
                );
        addTimeline(
                entity,
                "STARTED",
                oldStatus,
                entity.getStatus(),
                "Bắt đầu bảo trì/sửa chữa"
        );

        return mapper.toResponse(entity);
    }

    public WorkOrderResponse waitingParts(
            Long id
    ) {

        MaintenanceWorkOrder entity =
                getAccessibleWorkOrder(id);

        requireStatus(
                entity,
                WorkOrderStatus.IN_PROGRESS
        );

        WorkOrderStatus oldStatus =
                entity.getStatus();

        entity.setStatus(
                WorkOrderStatus.WAITING_PARTS
        );

        addTimeline(
                entity,
                "WAITING_PARTS",
                oldStatus,
                entity.getStatus(),
                "Chờ linh kiện"
        );

        return mapper.toResponse(entity);
    }

    public WorkOrderResponse resume(
            Long id
    ) {

        MaintenanceWorkOrder entity =
                getAccessibleWorkOrder(id);

        requireStatus(
                entity,
                WorkOrderStatus.WAITING_PARTS
        );

        WorkOrderStatus oldStatus =
                entity.getStatus();

        entity.setStatus(
                WorkOrderStatus.IN_PROGRESS
        );

        addTimeline(
                entity,
                "RESUMED",
                oldStatus,
                entity.getStatus(),
                "Tiếp tục sửa chữa"
        );

        return mapper.toResponse(entity);
    }

    public WorkOrderResponse complete(
            Long id,
            CompleteWorkOrderRequest dto
    ) {

        MaintenanceWorkOrder entity =
                getAccessibleWorkOrder(id);

        requireStatus(
                entity,
                WorkOrderStatus.IN_PROGRESS
        );

        CurrentUser current =
                currentUserService
                        .getCurrentUser();

        WorkOrderStatus oldStatus =
                entity.getStatus();

        entity.setDiagnosis(
                dto.diagnosis()
        );

        entity.setActionTaken(
                dto.actionTaken()
        );

        entity.setResult(
                dto.result()
        );

        entity.setResultNotes(
                dto.resultNotes()
        );

        entity.setActualCompleteAt(
                LocalDateTime.now()
        );

        entity.setCompletedByUserId(
                current.userId()
        );

        entity.setStatus(
                WorkOrderStatus.COMPLETED
        );

        addTimeline(
                entity,
                "COMPLETED",
                oldStatus,
                entity.getStatus(),
                "Kỹ thuật báo hoàn thành"
        );

        return mapper.toResponse(entity);
    }

    public WorkOrderResponse close(
            Long id
    ) {

        MaintenanceWorkOrder entity =
                getAccessibleWorkOrder(id);

        requireStatus(
                entity,
                WorkOrderStatus.COMPLETED
        );

        if (entity.getResult() == null) {
            throw new BusinessException(
                    "Work Order chưa có kết quả"
            );
        }

        CurrentUser current =
                currentUserService
                        .getCurrentUser();

        WorkOrderStatus oldStatus =
                entity.getStatus();

        entity.setStatus(
                WorkOrderStatus.CLOSED
        );

        entity.setClosedAt(
                LocalDateTime.now()
        );

        entity.setClosedByUserId(
                current.userId()
        );

        /*
         * Đồng bộ trạng thái thiết bị sang Inventory.
         */
        syncFinalEquipmentState(
                entity
        );

        /*
         * Nếu Work Order này được tạo từ Maintenance Request
         * có nguồn Customer Issue thì đóng luôn Customer Issue.
         */
        if (entity.getRequestId() != null) {

            customerIssueRepository
                    .findByMaintenanceRequestId(
                            entity.getRequestId()
                    )
                    .ifPresent(
                            issue -> {

                                issue.setStatus(
                                        CustomerIssueStatus.RESOLVED
                                );

                                issue.setResolvedAt(
                                        LocalDateTime.now()
                                );

                                issue.setResolutionNote(
                                        entity.getResultNotes()
                                );
                            }
                    );
        }

        addTimeline(
                entity,
                "CLOSED",
                oldStatus,
                entity.getStatus(),
                "Đóng Work Order"
        );

        return mapper.toResponse(entity);
    }

    public WorkOrderResponse cancel(
            Long id,
            String reason
    ) {

        MaintenanceWorkOrder entity =
                getAccessibleWorkOrder(id);

        if (
                entity.getStatus()
                        != WorkOrderStatus.OPEN
                        &&
                        entity.getStatus()
                                != WorkOrderStatus.ASSIGNED
        ) {

            throw new BusinessException(
                    "Chỉ được hủy Work Order ở OPEN hoặc ASSIGNED"
            );
        }

        CurrentUser current =
                currentUserService
                        .getCurrentUser();

        WorkOrderStatus oldStatus =
                entity.getStatus();

        entity.setStatus(
                WorkOrderStatus.CANCELLED
        );

        entity.setCancelledByUserId(
                current.userId()
        );

        entity.setCancelledAt(
                LocalDateTime.now()
        );

        entity.setCancelReason(
                reason
        );

        addTimeline(
                entity,
                "CANCELLED",
                oldStatus,
                entity.getStatus(),
                reason
        );

        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<WorkOrderTimelineResponse>
    timeline(
            Long workOrderId
    ) {

        getAccessibleWorkOrder(
                workOrderId
        );

        return timelineRepository
                .findByWorkOrderIdOrderByCreatedAtAsc(
                        workOrderId
                )
                .stream()
                .map(
                        mapper::toTimelineResponse
                )
                .toList();
    }

    private MaintenanceWorkOrder
    getAccessibleWorkOrder(
            Long id
    ) {

        MaintenanceWorkOrder entity =
                workOrderRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Không tìm thấy Work Order id="
                                                        + id
                                        )
                        );

        currentUserService
                .requireOrganization(
                        entity.getOrganizationId()
                );

        currentUserService
                .requireBranch(
                        entity.getBranchId()
                );

        return entity;
    }

    private void requireStatus(
            MaintenanceWorkOrder entity,
            WorkOrderStatus expected
    ) {

        if (
                entity.getStatus()
                        != expected
        ) {

            throw new BusinessException(
                    "Trạng thái hiện tại là "
                            + entity.getStatus()
                            + ", yêu cầu "
                            + expected
            );
        }
    }

    private void addTimeline(

            MaintenanceWorkOrder entity,

            String eventType,

            WorkOrderStatus fromStatus,

            WorkOrderStatus toStatus,

            String message
    ) {

        CurrentUser current =
                currentUserService
                        .getCurrentUser();

        WorkOrderTimeline timeline =
                WorkOrderTimeline
                        .builder()

                        .workOrderId(
                                entity.getId()
                        )

                        .eventType(
                                eventType
                        )

                        .fromStatus(
                                fromStatus == null
                                        ? null
                                        : fromStatus.name()
                        )

                        .toStatus(
                                toStatus == null
                                        ? null
                                        : toStatus.name()
                        )

                        .actorUserId(
                                current.userId()
                        )

                        .actorType(
                                "USER"
                        )

                        .message(
                                message
                        )

                        .build();

        timelineRepository.save(
                timeline
        );
    }

    private void syncFinalEquipmentState(
            MaintenanceWorkOrder entity
    ) {

        WorkOrderResult result =
                entity.getResult();

        if (
                result == WorkOrderResult.REPAIRED
                        ||
                        result == WorkOrderResult.NO_FAULT_FOUND
        ) {

            inventoryClient
                    .changeEquipmentStatus(
                            entity.getEquipmentId(),
                            "AVAILABLE"
                    );

            return;
        }

        if (
                result == WorkOrderResult.NOT_REPAIRABLE
                        ||
                        result == WorkOrderResult.UNSAFE
        ) {

            inventoryClient
                    .changeEquipmentStatus(
                            entity.getEquipmentId(),
                            "DAMAGED"
                    );

            return;
        }

        /*
         * PARTIALLY_REPAIRED:
         * vẫn giữ thiết bị ở trạng thái bảo trì.
         */
        inventoryClient
                .changeEquipmentStatus(
                        entity.getEquipmentId(),
                        "MAINTENANCE"
                );
    }

    private String generateCode() {

        return "WO-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10)
                .toUpperCase();
    }
}