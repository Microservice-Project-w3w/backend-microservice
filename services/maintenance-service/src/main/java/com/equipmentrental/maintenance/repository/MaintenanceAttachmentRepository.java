package com.equipmentrental.maintenance.repository;

import com.equipmentrental.maintenance.entity.MaintenanceAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceAttachmentRepository
        extends JpaRepository<MaintenanceAttachment, Long> {

    List<MaintenanceAttachment>
    findByWorkOrderIdOrderByUploadedAtAsc(
            Long workOrderId
    );
}