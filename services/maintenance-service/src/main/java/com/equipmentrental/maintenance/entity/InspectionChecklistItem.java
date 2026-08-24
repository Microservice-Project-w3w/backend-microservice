package com.equipmentrental.maintenance.entity;

import com.equipmentrental.maintenance.enums.InspectionItemResult;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inspection_checklist_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inspection_id", nullable = false)
    private Long inspectionId;

    @Column(name = "item_code")
    private String itemCode;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "expected_value")
    private String expectedValue;

    @Column(name = "actual_value")
    private String actualValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_result", nullable = false)
    private InspectionItemResult itemResult;

    @Column(length = 1000)
    private String note;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}