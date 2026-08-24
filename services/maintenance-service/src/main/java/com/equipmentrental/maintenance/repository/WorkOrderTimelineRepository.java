package com.equipmentrental.maintenance.repository;

import com.equipmentrental.maintenance.entity.WorkOrderTimeline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkOrderTimelineRepository
        extends JpaRepository<WorkOrderTimeline, Long> {

    List<WorkOrderTimeline>
    findByWorkOrderIdOrderByCreatedAtAsc(
            Long workOrderId
    );
}