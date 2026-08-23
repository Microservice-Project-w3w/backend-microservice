package com.equipmentrental.maintenance.service;

import com.equipmentrental.maintenance.dto.response.*;
import com.equipmentrental.maintenance.entity.MaintenanceCost;
import com.equipmentrental.maintenance.entity.MaintenanceWorkOrder;
import com.equipmentrental.maintenance.enums.CostType;
import com.equipmentrental.maintenance.enums.WorkOrderStatus;
import com.equipmentrental.maintenance.repository.MaintenanceCostRepository;
import com.equipmentrental.maintenance.repository.MaintenanceWorkOrderRepository;
import com.equipmentrental.maintenance.security.CurrentUser;
import com.equipmentrental.maintenance.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaintenanceReportService {

    private final MaintenanceWorkOrderRepository
            workOrderRepository;

    private final MaintenanceCostRepository
            costRepository;

    private final CurrentUserService
            currentUserService;

    public List<EquipmentMaintenanceHistoryResponse>
    equipmentHistory(Long equipmentId) {

        CurrentUser current =
                currentUserService.getCurrentUser();

        return accessibleWorkOrders()
                .stream()
                .filter(
                        wo -> Objects.equals(
                                wo.getEquipmentId(),
                                equipmentId
                        )
                )
                .map(this::toHistoryResponse)
                .toList();
    }

    public EquipmentMaintenanceSummaryResponse
    equipmentSummary(Long equipmentId) {

        List<MaintenanceWorkOrder> workOrders =
                accessibleWorkOrders()
                        .stream()
                        .filter(
                                wo -> Objects.equals(
                                        wo.getEquipmentId(),
                                        equipmentId
                                )
                        )
                        .toList();

        long open =
                workOrders.stream()
                        .filter(this::isOpen)
                        .count();

        long completed =
                workOrders.stream()
                        .filter(
                                wo -> wo.getStatus()
                                        == WorkOrderStatus.COMPLETED
                        )
                        .count();

        long closed =
                workOrders.stream()
                        .filter(
                                wo -> wo.getStatus()
                                        == WorkOrderStatus.CLOSED
                        )
                        .count();

        long downtimeMinutes =
                workOrders.stream()
                        .mapToLong(
                                this::calculateDowntimeMinutes
                        )
                        .sum();

        List<Long> ids =
                workOrders.stream()
                        .map(MaintenanceWorkOrder::getId)
                        .toList();

        BigDecimal totalCost =
                ids.isEmpty()
                        ? BigDecimal.ZERO
                        : costRepository
                        .findByWorkOrderIdIn(ids)
                        .stream()
                        .map(MaintenanceCost::getAmount)
                        .filter(Objects::nonNull)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        LocalDateTime lastMaintenanceAt =
                workOrders.stream()
                        .map(
                                wo -> wo.getActualCompleteAt() != null
                                        ? wo.getActualCompleteAt()
                                        : wo.getCreatedAt()
                        )
                        .filter(Objects::nonNull)
                        .max(LocalDateTime::compareTo)
                        .orElse(null);

        return new EquipmentMaintenanceSummaryResponse(
                equipmentId,
                workOrders.size(),
                open,
                completed,
                closed,
                downtimeMinutes,
                totalCost,
                lastMaintenanceAt
        );
    }

    public List<OpenWorkOrderResponse>
    openWorkOrders(Long equipmentId) {

        return accessibleWorkOrders()
                .stream()
                .filter(
                        wo -> Objects.equals(
                                wo.getEquipmentId(),
                                equipmentId
                        )
                )
                .filter(this::isOpen)
                .map(this::toOpenResponse)
                .toList();
    }

    public MaintenanceDashboardSummaryResponse
    dashboardSummary() {

        List<MaintenanceWorkOrder> workOrders =
                accessibleWorkOrders();

        long open = countStatus(
                workOrders,
                WorkOrderStatus.OPEN
        );

        long assigned = countStatus(
                workOrders,
                WorkOrderStatus.ASSIGNED
        );

        long inProgress = countStatus(
                workOrders,
                WorkOrderStatus.IN_PROGRESS
        );

        long waitingParts = countStatus(
                workOrders,
                WorkOrderStatus.WAITING_PARTS
        );

        long completed = countStatus(
                workOrders,
                WorkOrderStatus.COMPLETED
        );

        long closed = countStatus(
                workOrders,
                WorkOrderStatus.CLOSED
        );

        LocalDateTime now =
                LocalDateTime.now();

        long overdue =
                workOrders.stream()
                        .filter(this::isOpen)
                        .filter(
                                wo ->
                                        wo.getExpectedCompleteAt()
                                                != null
                                                &&
                                                wo.getExpectedCompleteAt()
                                                        .isBefore(now)
                        )
                        .count();

        Set<Long> workOrderIds =
                workOrders.stream()
                        .map(MaintenanceWorkOrder::getId)
                        .collect(Collectors.toSet());

        BigDecimal totalCost =
                accessibleCosts()
                        .stream()
                        .filter(
                                cost ->
                                        workOrderIds.contains(
                                                cost.getWorkOrderId()
                                        )
                        )
                        .map(MaintenanceCost::getAmount)
                        .filter(Objects::nonNull)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        return new MaintenanceDashboardSummaryResponse(
                open,
                assigned,
                inProgress,
                waitingParts,
                overdue,
                completed,
                closed,
                totalCost
        );
    }

    public MaintenanceCostReportResponse
    costReport() {

        List<MaintenanceCost> costs =
                accessibleCosts();

        BigDecimal part =
                sumCost(costs, CostType.PART);

        BigDecimal labor =
                sumCost(costs, CostType.LABOR);

        BigDecimal outsource =
                sumCost(costs, CostType.OUTSOURCE);

        BigDecimal transport =
                sumCost(costs, CostType.TRANSPORT);

        BigDecimal other =
                sumCost(costs, CostType.OTHER);

        BigDecimal total =
                part.add(labor)
                        .add(outsource)
                        .add(transport)
                        .add(other);

        return new MaintenanceCostReportResponse(
                part,
                labor,
                outsource,
                transport,
                other,
                total
        );
    }

    public List<MaintenanceDowntimeReportResponse>
    downtimeReport() {

        Map<Long, List<MaintenanceWorkOrder>>
                grouped =
                accessibleWorkOrders()
                        .stream()
                        .collect(
                                Collectors.groupingBy(
                                        MaintenanceWorkOrder::getEquipmentId
                                )
                        );

        return grouped.entrySet()
                .stream()
                .map(
                        entry -> {

                            long downtime =
                                    entry.getValue()
                                            .stream()
                                            .mapToLong(
                                                    this::calculateDowntimeMinutes
                                            )
                                            .sum();

                            return new MaintenanceDowntimeReportResponse(
                                    entry.getKey(),
                                    entry.getValue().size(),
                                    downtime
                            );
                        }
                )
                .sorted(
                        Comparator.comparingLong(
                                MaintenanceDowntimeReportResponse
                                        ::downtimeMinutes
                        ).reversed()
                )
                .toList();
    }

    public List<FailureFrequencyResponse>
    failureFrequency() {

        /*
         * Tài liệu chỉ yêu cầu "thiết bị lỗi nhiều lần"
         * nhưng không quy định công thức.
         *
         * Ở đây tính số Work Order không bị CANCELLED
         * của từng thiết bị.
         */

        return accessibleWorkOrders()
                .stream()
                .filter(
                        wo ->
                                wo.getStatus()
                                        != WorkOrderStatus.CANCELLED
                )
                .collect(
                        Collectors.groupingBy(
                                MaintenanceWorkOrder::getEquipmentId,
                                Collectors.counting()
                        )
                )
                .entrySet()
                .stream()
                .map(
                        entry ->
                                new FailureFrequencyResponse(
                                        entry.getKey(),
                                        entry.getValue()
                                )
                )
                .sorted(
                        Comparator.comparingLong(
                                FailureFrequencyResponse
                                        ::failureCount
                        ).reversed()
                )
                .toList();
    }

    private List<MaintenanceWorkOrder>
    accessibleWorkOrders() {

        CurrentUser current =
                currentUserService.getCurrentUser();

        List<MaintenanceWorkOrder> list =
                workOrderRepository
                        .findByOrganizationIdOrderByCreatedAtDesc(
                                current.organizationId()
                        );

        if (current.hasRole("ADMIN")) {
            return list;
        }

        return list.stream()
                .filter(
                        wo ->
                                current.branchIds()
                                        .contains(
                                                wo.getBranchId()
                                        )
                )
                .toList();
    }

    private List<MaintenanceCost>
    accessibleCosts() {

        CurrentUser current =
                currentUserService.getCurrentUser();

        List<MaintenanceCost> list =
                costRepository
                        .findByOrganizationId(
                                current.organizationId()
                        );

        if (current.hasRole("ADMIN")) {
            return list;
        }

        return list.stream()
                .filter(
                        cost ->
                                current.branchIds()
                                        .contains(
                                                cost.getBranchId()
                                        )
                )
                .toList();
    }

    private boolean isOpen(
            MaintenanceWorkOrder wo
    ) {

        return wo.getStatus()
                != WorkOrderStatus.CLOSED
                &&
                wo.getStatus()
                        != WorkOrderStatus.CANCELLED;
    }

    private long countStatus(
            List<MaintenanceWorkOrder> list,
            WorkOrderStatus status
    ) {

        return list.stream()
                .filter(
                        wo -> wo.getStatus() == status
                )
                .count();
    }

    private long calculateDowntimeMinutes(
            MaintenanceWorkOrder wo
    ) {

        if (wo.getActualStartAt() == null) {
            return 0;
        }

        LocalDateTime end =
                wo.getActualCompleteAt() != null
                        ? wo.getActualCompleteAt()
                        : LocalDateTime.now();

        if (end.isBefore(wo.getActualStartAt())) {
            return 0;
        }

        return Duration.between(
                wo.getActualStartAt(),
                end
        ).toMinutes();
    }

    private BigDecimal sumCost(
            List<MaintenanceCost> costs,
            CostType type
    ) {

        return costs.stream()
                .filter(
                        cost ->
                                cost.getCostType() == type
                )
                .map(MaintenanceCost::getAmount)
                .filter(Objects::nonNull)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    private EquipmentMaintenanceHistoryResponse
    toHistoryResponse(
            MaintenanceWorkOrder wo
    ) {

        return new EquipmentMaintenanceHistoryResponse(
                wo.getId(),
                wo.getWorkOrderCode(),
                wo.getEquipmentId(),
                wo.getBranchId(),
                wo.getTitle(),
                wo.getStatus(),
                wo.getPriority(),
                wo.getDiagnosis(),
                wo.getActionTaken(),
                wo.getResult(),
                wo.getActualStartAt(),
                wo.getActualCompleteAt(),
                wo.getCreatedAt()
        );
    }

    private OpenWorkOrderResponse
    toOpenResponse(
            MaintenanceWorkOrder wo
    ) {

        return new OpenWorkOrderResponse(
                wo.getId(),
                wo.getWorkOrderCode(),
                wo.getEquipmentId(),
                wo.getBranchId(),
                wo.getTitle(),
                wo.getStatus(),
                wo.getPriority(),
                wo.getAssignedUserId(),
                wo.getExpectedStartAt(),
                wo.getExpectedCompleteAt(),
                wo.getActualStartAt()
        );
    }
}