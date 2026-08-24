package com.equipmentrental.maintenance.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.equipmentrental.maintenance.client.RentalClient;
import com.equipmentrental.maintenance.dto.request.CreateCustomerIssueRequest;
import com.equipmentrental.maintenance.exception.ForbiddenException;
import com.equipmentrental.maintenance.repository.CustomerIssueRepository;
import com.equipmentrental.maintenance.repository.MaintenanceRequestRepository;
import com.equipmentrental.maintenance.service.CustomerIssueService;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class CustomerIssueScopeTest {

    private Long listedCustomerId;
    private boolean issueSaved;
    private RentalClient.RentalOwnershipResponse ownershipResponse;
    private CustomerIssueService service;

    @BeforeEach
    void setUp() {

        CustomerIssueRepository issueRepository = repositoryProxy(
                CustomerIssueRepository.class,
                (methodName, args) -> {
                    if ("findByCustomerIdOrderByCreatedAtDesc".equals(methodName)) {
                        listedCustomerId = (Long) args[0];
                        return List.of();
                    }
                    if ("save".equals(methodName)) {
                        issueSaved = true;
                        return args[0];
                    }
                    return defaultValue(methodName);
                }
        );

        MaintenanceRequestRepository requestRepository = repositoryProxy(
                MaintenanceRequestRepository.class,
                (methodName, args) -> defaultValue(methodName)
        );

        RentalClient rentalClient = new RentalClient("http://localhost") {
            @Override
            public RentalOwnershipResponse verifyOwnership(
                    Long rentalOrderId,
                    Long equipmentId
            ) {
                return ownershipResponse;
            }
        };

        service = new CustomerIssueService(
                issueRepository,
                new CurrentUserService(),
                rentalClient,
                requestRepository
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ownListUsesCustomerIdClaim() {

        authenticateCustomer(7L, 10L, 1001L);

        service.listOwn();

        assertThat(listedCustomerId).isEqualTo(1001L);
        assertThat(listedCustomerId).isNotEqualTo(7L);
    }

    @Test
    void createRejectsRentalOwnedByAnotherCustomer() {

        authenticateCustomer(7L, 10L, 1001L);

        CreateCustomerIssueRequest request =
                new CreateCustomerIssueRequest(
                        500L,
                        900L,
                        "Thiết bị lỗi",
                        "Không thể khởi động",
                        null
                );

        ownershipResponse = new RentalClient.RentalOwnershipResponse(
                true,
                500L,
                1002L,
                10L,
                101L,
                900L
        );

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ForbiddenException.class);

        assertThat(issueSaved).isFalse();
    }

    @SuppressWarnings("unchecked")
    private <T> T repositoryProxy(
            Class<T> repositoryType,
            RepositoryCall call
    ) {
        return (T) Proxy.newProxyInstance(
                repositoryType.getClassLoader(),
                new Class<?>[]{repositoryType},
                (proxy, method, args) -> call.invoke(
                        method.getName(),
                        args == null ? new Object[0] : args
                )
        );
    }

    private Object defaultValue(String methodName) {
        if (methodName.startsWith("find")) {
            return Optional.empty();
        }
        if (methodName.startsWith("exists")) {
            return false;
        }
        return null;
    }

    private void authenticateCustomer(
            Long userId,
            Long organizationId,
            Long customerId
    ) {

        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .claim("userId", userId)
                .claim("roles", List.of("CUSTOMER"))
                .claim("permissions", List.of(
                        "maintenance.incident.create",
                        "maintenance.incident.read"
                ))
                .claim("organizationId", organizationId)
                .claim("branchIds", List.of())
                .claim("customerId", customerId)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(jwt)
        );
    }

    @FunctionalInterface
    private interface RepositoryCall {
        Object invoke(String methodName, Object[] args);
    }
}
