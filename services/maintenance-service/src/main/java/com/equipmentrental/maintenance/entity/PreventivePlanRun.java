package com.equipmentrental.maintenance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "preventive_plan_runs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreventivePlanRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "work_order_id")
    private Long workOrderId;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "generated_by_user_id")
    private Long generatedByUserId;

    @Column(name = "generated_by_type", nullable = false, length = 20)
    private String generatedByType;

    @Column(name = "generated_at", insertable = false, updatable = false)
    private LocalDateTime generatedAt;
}
