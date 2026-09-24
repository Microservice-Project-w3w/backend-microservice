package com.equipmentrental.maintenance.security;

import com.equipmentrental.maintenance.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

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

        Long userId = toLong(jwt.getClaim("userId"));

        if (userId == null) {
            userId = toLong(jwt.getSubject());
        }

        if (userId == null) {
            throw new ForbiddenException(
                    "JWT không có userId hợp lệ"
            );
        }

        Long organizationId =
                toLong(jwt.getClaim("organizationId"));

        Long customerId =
                toLong(jwt.getClaim("customerId"));

        Set<Long> branchIds =
                toLongSet(jwt.getClaim("branchIds"));

        Set<String> roles =
                toStringSet(jwt.getClaim("roles"));

        Set<String> permissions =
                toStringSet(jwt.getClaim("permissions"));

        return new CurrentUser(
                userId,
                organizationId,
                branchIds,
                customerId,
                roles,
                permissions
        );
    }

    public void requireOrganization(
            Long organizationId
    ) {

        CurrentUser current =
                getCurrentUser();

        if (current.hasRole("ADMIN")) {
            return;
        }

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

    public void requireCustomer(
            Long customerId
    ) {

        CurrentUser current =
                getCurrentUser();

        if (
                current.customerId() == null
                        ||
                        !Objects.equals(
                                current.customerId(),
                                customerId
                        )
        ) {
            throw new ForbiddenException(
                    "Không được truy cập dữ liệu của customer khác"
            );
        }
    }

    private Long toLong(Object value) {

        if (value instanceof Number number) {
            return number.longValue();
        }

        if (value instanceof String text) {
            try {
                return Long.valueOf(text.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    private Set<Long> toLongSet(Object value) {

        Set<Long> values = new LinkedHashSet<>();

        if (value instanceof Collection<?> collection) {
            collection.forEach(item -> addLong(values, item));
        } else {
            addLong(values, value);
        }

        return Set.copyOf(values);
    }

    private void addLong(Set<Long> target, Object value) {

        Long converted = toLong(value);

        if (converted != null) {
            target.add(converted);
        }
    }

    private Set<String> toStringSet(Object value) {

        Set<String> values = new LinkedHashSet<>();

        if (value instanceof Collection<?> collection) {
            collection.forEach(item -> addString(values, item));
        } else {
            addString(values, value);
        }

        return Set.copyOf(values);
    }

    private void addString(Set<String> target, Object value) {

        if (value == null) {
            return;
        }

        String converted = String.valueOf(value).trim();

        if (!converted.isEmpty()) {
            target.add(converted);
        }
    }
}
