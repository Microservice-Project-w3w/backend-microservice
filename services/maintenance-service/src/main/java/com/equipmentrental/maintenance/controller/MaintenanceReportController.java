package com.equipmentrental.maintenance.controller;

import com.equipmentrental.maintenance.dto.response.*;
import com.equipmentrental.maintenance.service.MaintenanceReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maintenance")
@RequiredArgsConstructor
public class MaintenanceReportController {

    private final MaintenanceReportService service;

    @GetMapping(
            "/equipment/{equipmentId}/history"
    )
    @PreAuthorize(
            "hasAnyRole('ADMIN','MANAGER','OPERATIONS_STAFF','SALES_STAFF')"
    )
    public List<EquipmentMaintenanceHistoryResponse>
    history(
            @PathVariable Long equipmentId
    ) {

        return service.equipmentHistory(
                equipmentId
        );
    }

    @GetMapping(
            "/equipment/{equipmentId}/summary"
    )
    @PreAuthorize(
            "hasAnyRole('ADMIN','MANAGER','OPERATIONS_STAFF','SALES_STAFF')"
    )
    public EquipmentMaintenanceSummaryResponse
    summary(
            @PathVariable Long equipmentId
    ) {

        return service.equipmentSummary(
                equipmentId
        );
    }

    @GetMapping(
            "/equipment/{equipmentId}/open-work-orders"
    )
    @PreAuthorize(
            "hasAnyRole('ADMIN','MANAGER','OPERATIONS_STAFF','SALES_STAFF')"
    )
    public List<OpenWorkOrderResponse>
    openWorkOrders(
            @PathVariable Long equipmentId
    ) {

        return service.openWorkOrders(
                equipmentId
        );
    }

    @GetMapping("/dashboard/summary")
    @PreAuthorize(
            "hasAnyRole('ADMIN','MANAGER','OPERATIONS_STAFF')"
    )
    public MaintenanceDashboardSummaryResponse
    dashboard() {

        return service.dashboardSummary();
    }

    @GetMapping("/reports/costs")
    @PreAuthorize(
            "hasAnyRole('ADMIN','MANAGER','ACCOUNTANT')"
    )
    public MaintenanceCostReportResponse
    costReport() {

        return service.costReport();
    }

    @GetMapping("/reports/downtime")
    @PreAuthorize(
            "hasAnyRole('ADMIN','MANAGER')"
    )
    public List<MaintenanceDowntimeReportResponse>
    downtime() {

        return service.downtimeReport();
    }

    @GetMapping(
            "/reports/failure-frequency"
    )
    @PreAuthorize(
            "hasAnyRole('ADMIN','MANAGER','OPERATIONS_STAFF')"
    )
    public List<FailureFrequencyResponse>
    failureFrequency() {

        return service.failureFrequency();
    }
}