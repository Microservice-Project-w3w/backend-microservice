package com.equipmentrental.billing.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.equipmentrental.billing.dto.response.InvoiceResponse;
import com.equipmentrental.common.security.CurrentUserProvider;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class BillingDataScopeGuardTest {

    private final BillingDataScopeGuard guard = new BillingDataScopeGuard(
            new CurrentUserProvider(), null, null, null, null);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void accountantIsLimitedToOrganization() {
        authenticate("ACCOUNTANT", 10L, List.of(), null);

        assertThat(guard.canAccess(10L, 101L, 1001L)).isTrue();
        assertThat(guard.canAccess(20L, 101L, 1001L)).isFalse();
    }

    @Test
    void managerIsLimitedToAssignedBranches() {
        authenticate("MANAGER", 10L, List.of(101L), null);

        List<InvoiceResponse> result = guard.filterInvoices(List.of(
                invoice(1L, 10L, 101L, 1001L),
                invoice(2L, 10L, 202L, 1002L)));

        assertThat(result).extracting(InvoiceResponse::getId).containsExactly(1L);
    }

    @Test
    void customerIsLimitedToOwnBillingRecords() {
        authenticate("CUSTOMER", 10L, List.of(), 1001L);

        assertThat(guard.canAccess(10L, 101L, 1001L)).isTrue();
        assertThat(guard.canAccess(10L, 101L, 1002L)).isFalse();
    }

    private InvoiceResponse invoice(Long id, Long organizationId, Long branchId, Long customerId) {
        InvoiceResponse response = new InvoiceResponse();
        response.setId(id);
        response.setOrganizationId(organizationId);
        response.setBranchId(branchId);
        response.setCustomerId(customerId);
        return response;
    }

    private void authenticate(
            String role, Long organizationId, List<Long> branchIds, Long customerId) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("7")
                .claim("roles", List.of(role))
                .claim("organizationId", organizationId)
                .claim("branchIds", branchIds);
        if (customerId != null) {
            builder.claim("customerId", customerId);
        }
        SecurityContextHolder.getContext()
                .setAuthentication(new JwtAuthenticationToken(builder.build()));
    }
}
