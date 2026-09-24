package com.equipmentrental.billing.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.equipmentrental.billing.controller.BillingController;
import com.equipmentrental.billing.controller.DebtController;
import com.equipmentrental.billing.controller.DepositController;
import com.equipmentrental.billing.controller.InternalBillingController;
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

class BillingAuthorizationCoverageTest {

    @Test
    void everyBusinessEndpointHasMethodOrClassAuthorization() {
        List<Class<?>> controllers = List.of(
                BillingController.class,
                DebtController.class,
                DepositController.class,
                InternalBillingController.class);

        List<String> unprotected = controllers.stream()
                .flatMap(controller -> Arrays.stream(controller.getDeclaredMethods())
                        .filter(this::isRequestMapping)
                        .filter(method -> !hasAuthorization(controller, method))
                        .map(method -> controller.getSimpleName() + "#" + method.getName()))
                .toList();

        assertThat(unprotected).isEmpty();
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
