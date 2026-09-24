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
            "hasAuthority('maintenance.history.read')"
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
            "hasAuthority('maintenance.history.read')"
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
            "hasAuthority('maintenance.history.read')"
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
            "hasAuthority('maintenance.repair.read')"
    )
    public MaintenanceDashboardSummaryResponse
    dashboard() {

        return service.dashboardSummary();
    }

    @GetMapping("/reports/costs")
    @PreAuthorize(
            "hasAuthority('maintenance.history.read')"
    )
    public MaintenanceCostReportResponse
    costReport() {

        return service.costReport();
    }

    @GetMapping("/reports/downtime")
    @PreAuthorize(
            "hasAuthority('maintenance.history.read')"
    )
    public List<MaintenanceDowntimeReportResponse>
    downtime() {

        return service.downtimeReport();
    }

    @GetMapping(
            "/reports/failure-frequency"
    )
    @PreAuthorize(
            "hasAuthority('maintenance.history.read')"
    )
    public List<FailureFrequencyResponse>
    failureFrequency() {

        return service.failureFrequency();
    }
}
