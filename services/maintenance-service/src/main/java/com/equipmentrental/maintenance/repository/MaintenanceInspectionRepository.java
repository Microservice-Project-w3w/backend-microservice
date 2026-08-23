package com.equipmentrental.maintenance.repository;

import com.equipmentrental.maintenance.entity.MaintenanceInspection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceInspectionRepository
        extends JpaRepository<MaintenanceInspection, Long> {

    List<MaintenanceInspection>
    findByWorkOrderIdOrderByInspectedAtDesc(Long workOrderId);
}