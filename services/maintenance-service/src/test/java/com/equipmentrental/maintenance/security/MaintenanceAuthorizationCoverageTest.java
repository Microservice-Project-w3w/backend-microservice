package com.equipmentrental.maintenance.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.equipmentrental.maintenance.controller.CustomerIssueController;
import com.equipmentrental.maintenance.controller.CustomerIssueOperationsController;
import com.equipmentrental.maintenance.controller.InspectionController;
import com.equipmentrental.maintenance.controller.InternalMaintenanceController;
import com.equipmentrental.maintenance.controller.MaintenanceAttachmentController;
import com.equipmentrental.maintenance.controller.MaintenanceCostController;
import com.equipmentrental.maintenance.controller.MaintenanceReportController;
import com.equipmentrental.maintenance.controller.MaintenanceRequestController;
import com.equipmentrental.maintenance.controller.PartUsageController;
import com.equipmentrental.maintenance.controller.PreventiveMaintenancePlanController;
import com.equipmentrental.maintenance.controller.WorkOrderController;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

class MaintenanceAuthorizationCoverageTest {

    private static final List<Class<?>> BUSINESS_CONTROLLERS = List.of(
            CustomerIssueController.class,
            CustomerIssueOperationsController.class,
            InspectionController.class,
            InternalMaintenanceController.class,
            MaintenanceAttachmentController.class,
            MaintenanceCostController.class,
            MaintenanceReportController.class,
            MaintenanceRequestController.class,
            PartUsageController.class,
            PreventiveMaintenancePlanController.class,
            WorkOrderController.class);

    @Test
    void everyBusinessEndpointHasAuthorization() {

        List<String> unprotected = BUSINESS_CONTROLLERS.stream()
                .flatMap(controller -> Arrays.stream(controller.getDeclaredMethods())
                        .filter(this::isRequestMapping)
                        .filter(method -> !hasAuthorization(controller, method))
                        .map(method -> controller.getSimpleName() + "#" + method.getName()))
                .toList();

        assertThat(unprotected).isEmpty();
    }

    @Test
    void internalEndpointsUseRealJwtPermissions() {

        List<String> invalidExpressions = Arrays.stream(
                        InternalMaintenanceController.class.getDeclaredMethods())
                .filter(this::isRequestMapping)
                .map(method -> AnnotatedElementUtils.findMergedAnnotation(
                        method,
                        PreAuthorize.class
                ))
                .map(PreAuthorize::value)
                .filter(expression -> expression.contains("SERVICE_INTERNAL"))
                .toList();

        assertThat(invalidExpressions).isEmpty();
    }

    private boolean isRequestMapping(Method method) {
        return AnnotatedElementUtils.hasAnnotation(method, GetMapping.class)
                || AnnotatedElementUtils.hasAnnotation(method, PostMapping.class)
                || AnnotatedElementUtils.hasAnnotation(method, PutMapping.class)
                || AnnotatedElementUtils.hasAnnotation(method, PatchMapping.class)
                || AnnotatedElementUtils.hasAnnotation(method, DeleteMapping.class);
    }

    private boolean hasAuthorization(Class<?> controller, Method method) {
        return AnnotatedElementUtils.hasAnnotation(method, PreAuthorize.class)
                || AnnotatedElementUtils.hasAnnotation(controller, PreAuthorize.class);
    }
}
