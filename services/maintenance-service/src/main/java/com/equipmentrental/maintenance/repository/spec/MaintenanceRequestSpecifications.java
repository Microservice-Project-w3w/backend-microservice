package com.equipmentrental.maintenance.repository.spec;

import com.equipmentrental.maintenance.entity.MaintenanceRequest;
import com.equipmentrental.maintenance.enums.MaintenanceRequestStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Set;

public final class MaintenanceRequestSpecifications {

    private MaintenanceRequestSpecifications() {
    }

    public static Specification<MaintenanceRequest> organizationEquals(
            Long organizationId
    ) {
        return (root, query, cb) ->
                cb.equal(
                        root.get("organizationId"),
                        organizationId
                );
    }

    public static Specification<MaintenanceRequest> branchEquals(
            Long branchId
    ) {
        if (branchId == null) {
            return null;
        }

        return (root, query, cb) ->
                cb.equal(
                        root.get("branchId"),
                        branchId
                );
    }

    public static Specification<MaintenanceRequest> branchIn(
            Set<Long> branchIds
    ) {
        return (root, query, cb) ->
                root.get("branchId").in(branchIds);
    }

    public static Specification<MaintenanceRequest> equipmentEquals(
            Long equipmentId
    ) {
        if (equipmentId == null) {
            return null;
        }

        return (root, query, cb) ->
                cb.equal(
                        root.get("equipmentId"),
                        equipmentId
                );
    }

    public static Specification<MaintenanceRequest> statusEquals(
            MaintenanceRequestStatus status
    ) {
        if (status == null) {
            return null;
        }

        return (root, query, cb) ->
                cb.equal(
                        root.get("status"),
                        status
                );
    }

    public static Specification<MaintenanceRequest> createdFrom(
            LocalDateTime from
    ) {
        if (from == null) {
            return null;
        }

        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        from
                );
    }

    public static Specification<MaintenanceRequest> createdTo(
            LocalDateTime to
    ) {
        if (to == null) {
            return null;
        }

        return (root, query, cb) ->
                cb.lessThanOrEqualTo(
                        root.get("createdAt"),
                        to
                );
    }
}