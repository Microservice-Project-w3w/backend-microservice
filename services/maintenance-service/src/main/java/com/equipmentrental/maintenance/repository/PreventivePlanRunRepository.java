package com.equipmentrental.maintenance.repository;

import com.equipmentrental.maintenance.entity.PreventivePlanRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface PreventivePlanRunRepository
        extends JpaRepository<PreventivePlanRun, Long> {

    boolean existsByPlanIdAndDueDate(
            Long planId,
            LocalDate dueDate
    );
}