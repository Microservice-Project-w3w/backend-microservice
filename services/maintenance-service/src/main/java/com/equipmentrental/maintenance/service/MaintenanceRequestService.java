package com.equipmentrental.maintenance.service;

import com.equipmentrental.maintenance.dto.request.CreateMaintenanceRequestRequest;
import com.equipmentrental.maintenance.dto.request.UpdateMaintenanceRequestRequest;
import com.equipmentrental.maintenance.dto.response.MaintenanceRequestResponse;
import com.equipmentrental.maintenance.dto.response.WorkOrderSummaryResponse;
import com.equipmentrental.maintenance.entity.MaintenanceRequest;
import com.equipmentrental.maintenance.entity.MaintenanceWorkOrder;
import com.equipmentrental.maintenance.enums.CustomerIssueStatus;
import com.equipmentrental.maintenance.enums.MaintenanceRequestStatus;
import com.equipmentrental.maintenance.enums.MaintenanceSourceType;
import com.equipmentrental.maintenance.enums.WorkOrderStatus;
import com.equipmentrental.maintenance.exception.BusinessException;
import com.equipmentrental.maintenance.exception.ResourceNotFoundException;
import com.equipmentrental.maintenance.mapper.MaintenanceRequestMapper;
import com.equipmentrental.maintenance.repository.CustomerIssueRepository;
import com.equipmentrental.maintenance.repository.MaintenanceRequestRepository;
import com.equipmentrental.maintenance.repository.MaintenanceWorkOrderRepository;
import com.equipmentrental.maintenance.repository.spec.MaintenanceRequestSpecifications;
import com.equipmentrental.maintenance.security.CurrentUser;
import com.equipmentrental.maintenance.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceRequestService {

    private final MaintenanceRequestRepository
            requestRepository;

    private final MaintenanceWorkOrderRepository
            workOrderRepository;

    private final CustomerIssueRepository
            customerIssueRepository;

    private final CurrentUserService
            currentUserService;

    private final MaintenanceRequestMapper
            mapper;

    public MaintenanceRequestResponse create(
            CreateMaintenanceRequestRequest dto
    ) {

        CurrentUser current =
                currentUserService
                        .getCurrentUser();

        currentUserService
                .requireBranch(
                        dto.branchId()
                );

        MaintenanceRequest entity =
                MaintenanceRequest
                        .builder()

                        .requestCode(
                                generateCode("MR")
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

                        .rentalOrderId(
                                dto.rentalOrderId()
                        )

                        .sourceReferenceId(
                                dto.sourceReferenceId()
                        )

                        .sourceType(
                                dto.sourceType() == null
                                        ? MaintenanceSourceType.MANUAL
                                        : dto.sourceType()
                        )

                        .maintenanceType(
                                dto.maintenanceType()
                        )

                        .severity(
                                dto.severity()
                        )

                        .title(
                                dto.title()
                        )

                        .description(
                                dto.description()
                        )

                        .status(
                                MaintenanceRequestStatus.OPEN
                        )

                        .reportedByUserId(
                                current.userId()
                        )

                        .build();

        entity =
                requestRepository
                        .save(entity);

        return mapper
                .toResponse(entity);
    }

    @Transactional(readOnly = true)
    public Page<MaintenanceRequestResponse> list(

            MaintenanceRequestStatus status,

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
                    .requireBranch(
                            branchId
                    );
        }

        Specification<MaintenanceRequest>
                specification =
                Specification
                        .where(
                                MaintenanceRequestSpecifications
                                        .organizationEquals(
                                                current.organizationId()
                                        )
                        )

                        .and(
                                MaintenanceRequestSpecifications
                                        .statusEquals(
                                                status
                                        )
                        )

                        .and(
                                MaintenanceRequestSpecifications
                                        .branchEquals(
                                                branchId
                                        )
                        )

                        .and(
                                MaintenanceRequestSpecifications
                                        .equipmentEquals(
                                                equipmentId
                                        )
                        )

                        .and(
                                MaintenanceRequestSpecifications
                                        .createdFrom(
                                                from
                                        )
                        )

                        .and(
                                MaintenanceRequestSpecifications
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
                            MaintenanceRequestSpecifications
                                    .branchIn(
                                            current.branchIds()
                                    )
                    );
        }

        return requestRepository
                .findAll(
                        specification,
                        pageable
                )
                .map(
                        mapper::toResponse
                );
    }

    @Transactional(readOnly = true)
    public MaintenanceRequestResponse getById(
            Long id
    ) {

        return mapper.toResponse(
                getAccessibleRequest(id)
        );
    }

    public MaintenanceRequestResponse update(

            Long id,

            UpdateMaintenanceRequestRequest dto
    ) {

        MaintenanceRequest entity =
                getAccessibleRequest(id);

        if (
                entity.getStatus()
                        == MaintenanceRequestStatus.CANCELLED
                        ||
                        entity.getStatus()
                                == MaintenanceRequestStatus.CLOSED
        ) {

            throw new BusinessException(
                    "Không thể cập nhật request đã đóng hoặc hủy"
            );
        }

        if (
                dto.maintenanceType()
                        != null
        ) {

            entity.setMaintenanceType(
                    dto.maintenanceType()
            );
        }

        if (
                dto.severity()
                        != null
        ) {

            entity.setSeverity(
                    dto.severity()
            );
        }

        if (
                dto.title()
                        != null
        ) {

            entity.setTitle(
                    dto.title()
            );
        }

        if (
                dto.description()
                        != null
        ) {

            entity.setDescription(
                    dto.description()
            );
        }

        return mapper.toResponse(
                entity
        );
    }

    public MaintenanceRequestResponse cancel(

            Long id,

            String reason
    ) {

        MaintenanceRequest entity =
                getAccessibleRequest(id);

        if (
                entity.getStatus()
                        == MaintenanceRequestStatus.CANCELLED
        ) {

            throw new BusinessException(
                    "Request đã được hủy trước đó"
            );
        }

        if (
                entity.getStatus()
                        == MaintenanceRequestStatus.CLOSED
        ) {

            throw new BusinessException(
                    "Không thể hủy request đã CLOSED"
            );
        }

        if (
                entity.getStatus()
                        ==
                        MaintenanceRequestStatus
                                .CONVERTED_TO_WORK_ORDER
        ) {

            throw new BusinessException(
                    "Request đã tạo Work Order, không thể hủy trực tiếp"
            );
        }

        CurrentUser current =
                currentUserService
                        .getCurrentUser();

        entity.setStatus(
                MaintenanceRequestStatus
                        .CANCELLED
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

        return mapper.toResponse(
                entity
        );
    }

    public WorkOrderSummaryResponse createWorkOrder(
            Long requestId
    ) {

        MaintenanceRequest request =
                getAccessibleRequest(
                        requestId
                );

        if (
                request.getStatus()
                        ==
                        MaintenanceRequestStatus
                                .CANCELLED
                        ||
                        request.getStatus()
                                ==
                                MaintenanceRequestStatus
                                        .CLOSED
        ) {

            throw new BusinessException(
                    "Không thể tạo Work Order từ request đã kết thúc"
            );
        }

        if (
                workOrderRepository
                        .existsByRequestId(
                                requestId
                        )
        ) {

            throw new BusinessException(
                    "Request này đã có Work Order"
            );
        }

        CurrentUser current =
                currentUserService
                        .getCurrentUser();

        MaintenanceWorkOrder workOrder =
                MaintenanceWorkOrder
                        .builder()

                        .workOrderCode(
                                generateCode("WO")
                        )

                        .requestId(
                                request.getId()
                        )

                        .organizationId(
                                request.getOrganizationId()
                        )

                        .branchId(
                                request.getBranchId()
                        )

                        .equipmentId(
                                request.getEquipmentId()
                        )

                        .status(
                                WorkOrderStatus.OPEN
                        )

                        .priority(
                                request.getSeverity()
                        )

                        .title(
                                request.getTitle()
                        )

                        .description(
                                request.getDescription()
                        )

                        .createdByUserId(
                                current.userId()
                        )

                        .build();

        MaintenanceWorkOrder savedWorkOrder =
                workOrderRepository
                        .save(workOrder);

        request.setStatus(
                MaintenanceRequestStatus
                        .CONVERTED_TO_WORK_ORDER
        );

        customerIssueRepository
                .findByMaintenanceRequestId(
                        requestId
                )
                .ifPresent(
                        issue -> {

                            issue.setWorkOrderId(
                                    savedWorkOrder.getId()
                            );

                            issue.setStatus(
                                    CustomerIssueStatus.IN_PROGRESS
                            );
                        }
                );



        return mapper
                .toWorkOrderResponse(
                        workOrder
                );
    }

    private MaintenanceRequest getAccessibleRequest(
            Long id
    ) {

        MaintenanceRequest entity =
                requestRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Không tìm thấy Maintenance Request id="
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

    private String generateCode(
            String prefix
    ) {

        return prefix
                + "-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10)
                .toUpperCase();
    }
}