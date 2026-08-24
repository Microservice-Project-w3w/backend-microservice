package com.equipmentrental.maintenance.repository;

import com.equipmentrental.maintenance.entity.MaintenanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MaintenanceRequestRepository
        extends JpaRepository<MaintenanceRequest, Long>,
        JpaSpecificationExecutor<MaintenanceRequest> {
    boolean existsByEquipmentIdAndStatusIn(
            Long equipmentId,
            java.util.Collection<com.equipmentrental.maintenance.enums.MaintenanceRequestStatus> statuses
    );
}