package com.equipmentrental.maintenance.entity;

import com.equipmentrental.maintenance.enums.InspectionCondition;
import com.equipmentrental.maintenance.enums.InspectionResult;
import com.equipmentrental.maintenance.enums.InspectionStatus;
import com.equipmentrental.maintenance.enums.Severity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_inspections")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_order_id", nullable = false)
    private Long workOrderId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "inspection_condition", nullable = false)
    private InspectionCondition inspectionCondition;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false)
    private InspectionResult result;

    @Column(name = "cause", columnDefinition = "TEXT")
    private String cause;

    @Column(name = "recommendation", columnDefinition = "TEXT")
    private String recommendation;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InspectionStatus status;

    @Column(name = "inspected_by_user_id", nullable = false)
    private Long inspectedByUserId;

    @Column(name = "inspected_at", nullable = false)
    private LocalDateTime inspectedAt;

    @Column(name = "submitted_by_user_id")
    private Long submittedByUserId;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(
            name = "created_at",
            insertable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            insertable = false,
            updatable = false
    )
    private LocalDateTime updatedAt;
}