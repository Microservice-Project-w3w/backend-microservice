package com.equipmentrental.maintenance.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.equipmentrental.maintenance.config.SecurityConfig;
import com.equipmentrental.maintenance.exception.ForbiddenException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class MaintenanceSecurityTest {

    private final CurrentUserService currentUserService =
            new CurrentUserService();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void jwtConverterIncludesPermissionsAndRoles() {

        Jwt jwt = jwtBuilder()
                .claim("roles", List.of("OPERATIONS_STAFF"))
                .claim("permissions", List.of(
                        "maintenance.ticket.read",
                        "maintenance.repair.update"
                ))
                .build();

        JwtAuthenticationConverter converter =
                new SecurityConfig().jwtAuthenticationConverter();

        assertThat(converter.convert(jwt).getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder(
                        "ROLE_OPERATIONS_STAFF",
                        "maintenance.ticket.read",
                        "maintenance.repair.update"
                );
    }

    @Test
    void customerScopeUsesCustomerIdClaimInsteadOfUserId() {

        authenticate("CUSTOMER", 10L, List.of(), 1001L);

        CurrentUser current = currentUserService.getCurrentUser();

        assertThat(current.userId()).isEqualTo(7L);
        assertThat(current.customerId()).isEqualTo(1001L);
        assertThatCode(() -> currentUserService.requireCustomer(1001L))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> currentUserService.requireCustomer(7L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void operationsStaffCannotCrossOrganizationOrBranch() {

        authenticate("OPERATIONS_STAFF", 10L, List.of(101L), null);

        assertThatCode(() -> {
                    currentUserService.requireOrganization(10L);
                    currentUserService.requireBranch(101L);
                })
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> currentUserService.requireOrganization(20L))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> currentUserService.requireBranch(202L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void adminCanAccessAllOrganizationsAndBranches() {

        authenticate("ADMIN", 10L, List.of(), null);

        assertThatCode(() -> {
                    currentUserService.requireOrganization(20L);
                    currentUserService.requireBranch(202L);
                })
                .doesNotThrowAnyException();
    }

    private void authenticate(
            String role,
            Long organizationId,
            List<Long> branchIds,
            Long customerId
    ) {

        Jwt.Builder builder = jwtBuilder()
                .claim("userId", 7L)
                .claim("roles", List.of(role))
                .claim("permissions", List.of())
                .claim("organizationId", organizationId)
                .claim("branchIds", branchIds);

        if (customerId != null) {
            builder.claim("customerId", customerId);
        }

        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(builder.build())
        );
    }

    private Jwt.Builder jwtBuilder() {
        Instant now = Instant.now();
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("7")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300));
    }
}
