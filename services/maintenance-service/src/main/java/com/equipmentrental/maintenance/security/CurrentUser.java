package com.equipmentrental.maintenance.security;

import java.util.Set;

public record CurrentUser(

        Long userId,
        Long organizationId,
        Set<Long> branchIds,
        Long customerId,
        Set<String> roles,
        Set<String> permissions

) {

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }
}
