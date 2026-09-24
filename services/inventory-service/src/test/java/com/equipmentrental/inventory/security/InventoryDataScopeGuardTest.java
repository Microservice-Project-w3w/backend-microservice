package com.equipmentrental.inventory.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.equipmentrental.common.security.CurrentUserProvider;
import com.equipmentrental.common.security.DataScopeAuthorizer;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class InventoryDataScopeGuardTest {

    private final InventoryDataScopeGuard guard = new InventoryDataScopeGuard(
            new CurrentUserProvider(), new DataScopeAuthorizer());

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void employeeListsAreFilteredToAssignedBranches() {
        authenticate("OPERATIONS_STAFF", 10L, List.of(101L));

        List<Resource> result = guard.filterAssignedBranches(
                10L,
                List.of(new Resource(1L, 101L), new Resource(2L, 202L)),
                Resource::branchId);

        assertThat(result).extracting(Resource::id).containsExactly(1L);
        assertThatThrownBy(() -> guard.checkReadableBranch(10L, 202L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void customerCanReadOrganizationCatalogButCannotWriteAnotherBranch() {
        authenticate("CUSTOMER", 10L, List.of());

        guard.checkReadableBranch(10L, 202L);
        assertThatThrownBy(() -> guard.checkBranch(10L, 202L))
                .isInstanceOf(AccessDeniedException.class);
    }

    private void authenticate(String role, Long organizationId, List<Long> branchIds) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("7")
                .claim("roles", List.of(role))
                .claim("organizationId", organizationId)
                .claim("branchIds", branchIds)
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    private record Resource(Long id, Long branchId) {}
}
