package com.equipmentrental.maintenance.entity;

import com.equipmentrental.maintenance.enums.Severity;
import com.equipmentrental.maintenance.enums.WorkOrderResult;
import com.equipmentrental.maintenance.enums.WorkOrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_work_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceWorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "work_order_code",
            nullable = false,
            unique = true,
            length = 40
    )
    private String workOrderCode;

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "assigned_user_id")
    private Long assignedUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private WorkOrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private Severity priority;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "action_taken", columnDefinition = "TEXT")
    private String actionTaken;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "expected_start_at")
    private LocalDateTime expectedStartAt;

    @Column(name = "expected_complete_at")
    private LocalDateTime expectedCompleteAt;

    @Column(name = "actual_start_at")
    private LocalDateTime actualStartAt;

    @Column(name = "actual_complete_at")
    private LocalDateTime actualCompleteAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 40)
    private WorkOrderResult result;

    @Column(name = "result_notes", columnDefinition = "TEXT")
    private String resultNotes;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "completed_by_user_id")
    private Long completedByUserId;

    @Column(name = "closed_by_user_id")
    private Long closedByUserId;

    @Column(name = "cancelled_by_user_id")
    private Long cancelledByUserId;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

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