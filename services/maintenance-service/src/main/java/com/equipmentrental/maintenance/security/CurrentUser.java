package com.equipmentrental.maintenance.security;

import java.util.Set;

public record CurrentUser(

        Long userId,
        Long organizationId,
        Set<Long> branchIds,
        Set<String> roles

) {

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}