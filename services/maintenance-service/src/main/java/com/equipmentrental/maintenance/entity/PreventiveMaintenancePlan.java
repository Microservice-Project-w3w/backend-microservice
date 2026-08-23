package com.equipmentrental.maintenance.entity;

import com.equipmentrental.maintenance.enums.PreventiveFrequencyType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "preventive_maintenance_plans")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreventiveMaintenancePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_code", nullable = false, unique = true)
    private String planCode;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "equipment_id")
    private Long equipmentId;

    @Column(name = "equipment_type_id")
    private Long equipmentTypeId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_type", nullable = false)
    private PreventiveFrequencyType frequencyType;

    @Column(name = "frequency_value", nullable = false)
    private Integer frequencyValue;

    @Column(name = "last_maintenance_date")
    private LocalDate lastMaintenanceDate;

    @Column(name = "next_maintenance_date", nullable = false)
    private LocalDate nextMaintenanceDate;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "updated_by_user_id")
    private Long updatedByUserId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}