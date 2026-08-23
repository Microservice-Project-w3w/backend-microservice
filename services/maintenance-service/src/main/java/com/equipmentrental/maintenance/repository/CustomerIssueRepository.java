package com.equipmentrental.maintenance.repository;

import com.equipmentrental.maintenance.entity.CustomerIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CustomerIssueRepository
        extends JpaRepository<CustomerIssue, Long> {

    List<CustomerIssue>
    findByCustomerIdOrderByCreatedAtDesc(
            Long customerId
    );

    Optional<CustomerIssue>
    findByIdAndCustomerId(
            Long id,
            Long customerId
    );

    List<CustomerIssue>
    findByOrganizationIdOrderByCreatedAtDesc(
            Long organizationId
    );

    List<CustomerIssue>
    findByOrganizationIdAndBranchIdInOrderByCreatedAtDesc(
            Long organizationId,
            Collection<Long> branchIds
    );

    Optional<CustomerIssue>
    findByMaintenanceRequestId(
            Long maintenanceRequestId
    );
}