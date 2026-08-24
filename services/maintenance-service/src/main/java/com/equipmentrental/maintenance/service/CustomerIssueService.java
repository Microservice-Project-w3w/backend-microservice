package com.equipmentrental.maintenance.service;

import com.equipmentrental.maintenance.client.RentalClient;
import com.equipmentrental.maintenance.dto.request.CreateCustomerIssueRequest;
import com.equipmentrental.maintenance.dto.response.CustomerIssueResponse;
import com.equipmentrental.maintenance.entity.CustomerIssue;
import com.equipmentrental.maintenance.entity.MaintenanceRequest;
import com.equipmentrental.maintenance.enums.CustomerIssueStatus;
import com.equipmentrental.maintenance.enums.MaintenanceRequestStatus;
import com.equipmentrental.maintenance.enums.MaintenanceSourceType;
import com.equipmentrental.maintenance.enums.MaintenanceType;
import com.equipmentrental.maintenance.enums.Severity;
import com.equipmentrental.maintenance.exception.BusinessException;
import com.equipmentrental.maintenance.exception.ResourceNotFoundException;
import com.equipmentrental.maintenance.repository.CustomerIssueRepository;
import com.equipmentrental.maintenance.repository.MaintenanceRequestRepository;
import com.equipmentrental.maintenance.security.CurrentUser;
import com.equipmentrental.maintenance.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerIssueService {

    private final CustomerIssueRepository issueRepository;

    private final CurrentUserService currentUserService;

    private final RentalClient rentalClient;

    private final MaintenanceRequestRepository maintenanceRequestRepository;

    /*
     * CUSTOMER tạo báo cáo sự cố.
     */
    public CustomerIssueResponse create(
            CreateCustomerIssueRequest dto
    ) {

        RentalClient.RentalOwnershipResponse ownership =
                rentalClient.verifyOwnership(
                        dto.rentalOrderId(),
                        dto.equipmentId()
                );

        CustomerIssue issue =
                CustomerIssue.builder()

                        .issueCode(
                                generateIssueCode()
                        )

                        .organizationId(
                                ownership.organizationId()
                        )

                        .branchId(
                                ownership.branchId()
                        )

                        .customerId(
                                ownership.customerId()
                        )

                        .equipmentId(
                                ownership.equipmentId()
                        )

                        .rentalOrderId(
                                ownership.rentalOrderId()
                        )

                        .title(
                                dto.title()
                        )

                        .description(
                                dto.description()
                        )

                        .severity(
                                dto.severity() == null
                                        ? Severity.MEDIUM
                                        : dto.severity()
                        )

                        .status(
                                CustomerIssueStatus.REPORTED
                        )

                        .build();

        issue =
                issueRepository.save(issue);

        return toResponse(issue);
    }

    /*
     * CUSTOMER xem danh sách issue của chính mình.
     */
    @Transactional(readOnly = true)
    public List<CustomerIssueResponse> listOwn() {

        Long customerId =
                getCurrentCustomerId();

        return issueRepository
                .findByCustomerIdOrderByCreatedAtDesc(
                        customerId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /*
     * CUSTOMER xem chi tiết issue của chính mình.
     */
    @Transactional(readOnly = true)
    public CustomerIssueResponse getOwnById(
            Long id
    ) {

        Long customerId =
                getCurrentCustomerId();

        CustomerIssue issue =
                issueRepository
                        .findByIdAndCustomerId(
                                id,
                                customerId
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Không tìm thấy Customer Issue id="
                                                        + id
                                        )
                        );

        return toResponse(issue);
    }

    /*
     * ADMIN / OPERATIONS_STAFF xem danh sách Customer Issue.
     */
    @Transactional(readOnly = true)
    public List<CustomerIssueResponse> listForOperations() {

        CurrentUser current =
                currentUserService.getCurrentUser();

        List<CustomerIssue> issues;

        if (current.hasRole("ADMIN")) {

            issues =
                    issueRepository
                            .findByOrganizationIdOrderByCreatedAtDesc(
                                    current.organizationId()
                            );

        } else {

            issues =
                    issueRepository
                            .findByOrganizationIdAndBranchIdInOrderByCreatedAtDesc(
                                    current.organizationId(),
                                    current.branchIds()
                            );
        }

        return issues
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /*
     * ADMIN / OPERATIONS_STAFF xem chi tiết issue.
     */
    @Transactional(readOnly = true)
    public CustomerIssueResponse getForOperations(
            Long id
    ) {

        CustomerIssue issue =
                getAccessibleIssue(id);

        return toResponse(issue);
    }

    /*
     * OPERATIONS xác nhận issue CUSTOMER báo là hợp lệ.
     *
     * REPORTED -> VERIFIED
     */
    public CustomerIssueResponse verify(
            Long id
    ) {

        CustomerIssue issue =
                getAccessibleIssue(id);

        if (
                issue.getStatus()
                        != CustomerIssueStatus.REPORTED
        ) {

            throw new BusinessException(
                    "Chỉ được xác nhận Customer Issue ở trạng thái REPORTED"
            );
        }

        issue.setStatus(
                CustomerIssueStatus.VERIFIED
        );

        return toResponse(issue);
    }

    /*
     * Tạo Maintenance Request từ Customer Issue.
     *
     * VERIFIED
     *      ->
     * Maintenance Request OPEN
     *      +
     * Customer Issue IN_PROGRESS
     */
    public CustomerIssueResponse createMaintenanceRequest(
            Long id
    ) {

        CustomerIssue issue =
                getAccessibleIssue(id);

        if (
                issue.getStatus()
                        != CustomerIssueStatus.VERIFIED
        ) {

            throw new BusinessException(
                    "Customer Issue phải ở trạng thái VERIFIED trước khi tạo Maintenance Request"
            );
        }

        if (
                issue.getMaintenanceRequestId()
                        != null
        ) {

            throw new BusinessException(
                    "Customer Issue này đã có Maintenance Request"
            );
        }

        MaintenanceRequest maintenanceRequest =
                MaintenanceRequest.builder()

                        .requestCode(
                                generateMaintenanceRequestCode()
                        )

                        .organizationId(
                                issue.getOrganizationId()
                        )

                        .branchId(
                                issue.getBranchId()
                        )

                        .equipmentId(
                                issue.getEquipmentId()
                        )

                        .rentalOrderId(
                                issue.getRentalOrderId()
                        )

                        .sourceReferenceId(
                                issue.getIssueCode()
                        )

                        .sourceType(
                                MaintenanceSourceType.CUSTOMER_ISSUE
                        )

                        .maintenanceType(
                                MaintenanceType.CORRECTIVE
                        )

                        .severity(
                                issue.getSeverity()
                        )

                        .title(
                                issue.getTitle()
                        )

                        .description(
                                issue.getDescription()
                        )

                        .status(
                                MaintenanceRequestStatus.OPEN
                        )

                        .reportedByCustomerId(
                                issue.getCustomerId()
                        )

                        .build();

        maintenanceRequest =
                maintenanceRequestRepository.save(
                        maintenanceRequest
                );

        issue.setMaintenanceRequestId(
                maintenanceRequest.getId()
        );

        issue.setStatus(
                CustomerIssueStatus.IN_PROGRESS
        );

        return toResponse(issue);
    }

    /*
     * Kiểm tra issue thuộc đúng organization/branch
     * mà ADMIN/OPS hiện tại được phép truy cập.
     */
    private CustomerIssue getAccessibleIssue(
            Long id
    ) {

        CustomerIssue issue =
                issueRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Không tìm thấy Customer Issue id="
                                                        + id
                                        )
                        );

        currentUserService
                .requireOrganization(
                        issue.getOrganizationId()
                );

        currentUserService
                .requireBranch(
                        issue.getBranchId()
                );

        return issue;
    }

    /*
     * Hiện hệ thống test đang quy ước:
     *
     * CUSTOMER userId == customerId
     */
    private Long getCurrentCustomerId() {

        CurrentUser current =
                currentUserService.getCurrentUser();

        return current.userId();
    }

    private CustomerIssueResponse toResponse(
            CustomerIssue issue
    ) {

        return new CustomerIssueResponse(
                issue.getId(),
                issue.getIssueCode(),

                issue.getOrganizationId(),
                issue.getBranchId(),

                issue.getCustomerId(),
                issue.getEquipmentId(),
                issue.getRentalOrderId(),

                issue.getTitle(),
                issue.getDescription(),

                issue.getSeverity(),
                issue.getStatus(),

                issue.getMaintenanceRequestId(),
                issue.getWorkOrderId(),

                issue.getReportedAt(),
                issue.getResolvedAt(),
                issue.getResolutionNote(),

                issue.getCreatedAt(),
                issue.getUpdatedAt()
        );
    }

    private String generateIssueCode() {

        return "CI-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10)
                .toUpperCase();
    }

    private String generateMaintenanceRequestCode() {

        return "MR-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10)
                .toUpperCase();
    }
}