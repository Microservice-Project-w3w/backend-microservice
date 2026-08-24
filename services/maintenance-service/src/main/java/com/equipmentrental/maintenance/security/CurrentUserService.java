package com.equipmentrental.maintenance.security;

import com.equipmentrental.maintenance.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CurrentUserService {

    public CurrentUser getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null ||
                        !(authentication.getPrincipal() instanceof Jwt jwt)
        ) {
            throw new ForbiddenException(
                    "Không đọc được thông tin người dùng từ JWT"
            );
        }

        Long userId =
                Long.valueOf(jwt.getSubject());

        Number organizationClaim =
                jwt.getClaim("organizationId");

        Long organizationId =
                organizationClaim == null
                        ? null
                        : organizationClaim.longValue();

        List<Number> branchClaims =
                jwt.getClaim("branchIds");

        Set<Long> branchIds =
                branchClaims == null
                        ? Set.of()
                        : branchClaims
                        .stream()
                        .map(Number::longValue)
                        .collect(Collectors.toSet());

        List<String> roleClaims =
                jwt.getClaimAsStringList("roles");

        Set<String> roles =
                roleClaims == null
                        ? Set.of()
                        : new HashSet<>(roleClaims);

        return new CurrentUser(
                userId,
                organizationId,
                branchIds,
                roles
        );
    }

    public void requireOrganization(
            Long organizationId
    ) {

        CurrentUser current =
                getCurrentUser();

        if (
                !Objects.equals(
                        current.organizationId(),
                        organizationId
                )
        ) {
            throw new ForbiddenException(
                    "Không được truy cập dữ liệu của organization khác"
            );
        }
    }

    public void requireBranch(
            Long branchId
    ) {

        CurrentUser current =
                getCurrentUser();

        if (current.hasRole("ADMIN")) {
            return;
        }

        if (
                !current.branchIds()
                        .contains(branchId)
        ) {
            throw new ForbiddenException(
                    "Không có quyền truy cập branch này"
            );
        }
    }
}