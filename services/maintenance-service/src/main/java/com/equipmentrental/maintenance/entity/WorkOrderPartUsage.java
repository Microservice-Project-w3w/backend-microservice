package com.equipmentrental.maintenance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "work_order_part_usages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderPartUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_order_id", nullable = false)
    private Long workOrderId;

    @Column(name = "part_id")
    private Long partId;

    @Column(name = "part_code_snapshot")
    private String partCodeSnapshot;

    @Column(name = "part_name_snapshot", nullable = false)
    private String partNameSnapshot;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(length = 40)
    private String unit;

    @Column(name = "unit_cost", precision = 18, scale = 2)
    private BigDecimal unitCost;

    @Column(
            name = "total_cost",
            insertable = false,
            updatable = false,
            precision = 18,
            scale = 2
    )
    private BigDecimal totalCost;

    @Column(length = 1000)
    private String note;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}