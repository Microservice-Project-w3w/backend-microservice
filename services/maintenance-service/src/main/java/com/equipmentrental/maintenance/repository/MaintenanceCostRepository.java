package com.equipmentrental.maintenance.repository;

import com.equipmentrental.maintenance.entity.MaintenanceCost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceCostRepository
        extends JpaRepository<MaintenanceCost, Long> {

    List<MaintenanceCost>
    findByWorkOrderIdOrderByCreatedAtAsc(
            Long workOrderId
    );
    List<MaintenanceCost> findByOrganizationId(
            Long organizationId
    );

    List<MaintenanceCost> findByWorkOrderIdIn(
            List<Long> workOrderIds
    );
}