package com.equipmentrental.maintenance.repository;

import com.equipmentrental.maintenance.entity.MaintenanceWorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
public interface MaintenanceWorkOrderRepository
        extends JpaRepository<MaintenanceWorkOrder, Long>,
        JpaSpecificationExecutor<MaintenanceWorkOrder> {

    boolean existsByRequestId(Long requestId);
    List<MaintenanceWorkOrder>
    findByOrganizationIdAndEquipmentIdOrderByCreatedAtDesc(
            Long organizationId,
            Long equipmentId
    );

    List<MaintenanceWorkOrder>
    findByOrganizationIdOrderByCreatedAtDesc(
            Long organizationId
    );
    boolean existsByEquipmentIdAndStatusIn(
            Long equipmentId,
            java.util.Collection<com.equipmentrental.maintenance.enums.WorkOrderStatus> statuses
    );
}