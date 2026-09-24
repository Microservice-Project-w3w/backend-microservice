package com.equipmentrental.maintenance.repository;

import com.equipmentrental.maintenance.entity.PreventiveMaintenancePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PreventiveMaintenancePlanRepository
        extends JpaRepository<PreventiveMaintenancePlan, Long> {

    List<PreventiveMaintenancePlan>
    findByOrganizationIdOrderByNextMaintenanceDateAsc(
            Long organizationId
    );

    List<PreventiveMaintenancePlan>
    findByOrganizationIdAndActiveTrueAndNextMaintenanceDateLessThanEqualOrderByNextMaintenanceDateAsc(
            Long organizationId,
            LocalDate date
    );
}