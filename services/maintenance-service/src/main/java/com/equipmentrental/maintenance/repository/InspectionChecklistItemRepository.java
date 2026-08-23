package com.equipmentrental.maintenance.repository;

import com.equipmentrental.maintenance.entity.InspectionChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionChecklistItemRepository
        extends JpaRepository<InspectionChecklistItem, Long> {

    List<InspectionChecklistItem>
    findByInspectionIdOrderBySortOrderAsc(Long inspectionId);

    void deleteByInspectionId(Long inspectionId);
}