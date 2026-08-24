package com.equipmentrental.maintenance.entity;

import com.equipmentrental.maintenance.enums.MaintenanceRequestStatus;
import com.equipmentrental.maintenance.enums.MaintenanceSourceType;
import com.equipmentrental.maintenance.enums.MaintenanceType;
import com.equipmentrental.maintenance.enums.Severity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_code", nullable = false, unique = true, length = 40)
    private String requestCode;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "rental_order_id")
    private Long rentalOrderId;

    @Column(name = "source_reference_id", length = 100)
    private String sourceReferenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private MaintenanceSourceType sourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "maintenance_type", nullable = false, length = 30)
    private MaintenanceType maintenanceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private Severity severity;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MaintenanceRequestStatus status;

    @Column(name = "reported_by_user_id")
    private Long reportedByUserId;

    @Column(name = "reported_by_customer_id")
    private Long reportedByCustomerId;

    @Column(name = "cancelled_by_user_id")
    private Long cancelledByUserId;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "closed_by_user_id")
    private Long closedByUserId;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}