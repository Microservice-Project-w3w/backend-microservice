package com.equipmentrental.maintenance.repository;

import com.equipmentrental.maintenance.entity.WorkOrderPartUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkOrderPartUsageRepository
        extends JpaRepository<WorkOrderPartUsage, Long> {

    List<WorkOrderPartUsage>
    findByWorkOrderIdOrderByIdAsc(Long workOrderId);

    Optional<WorkOrderPartUsage>
    findByIdAndWorkOrderId(
            Long id,
            Long workOrderId
    );
}