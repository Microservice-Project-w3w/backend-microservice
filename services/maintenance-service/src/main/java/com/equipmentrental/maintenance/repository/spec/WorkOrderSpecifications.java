package com.equipmentrental.maintenance.repository.spec;

import com.equipmentrental.maintenance.entity.MaintenanceWorkOrder;
import com.equipmentrental.maintenance.enums.WorkOrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Set;

public final class WorkOrderSpecifications {

    private WorkOrderSpecifications() {
    }

    public static Specification<MaintenanceWorkOrder>
    organizationEquals(Long organizationId) {

        return (root, query, cb) ->
                cb.equal(
                        root.get("organizationId"),
                        organizationId
                );
    }

    public static Specification<MaintenanceWorkOrder>
    branchEquals(Long branchId) {

        if (branchId == null) {
            return null;
        }

        return (root, query, cb) ->
                cb.equal(
                        root.get("branchId"),
                        branchId
                );
    }

    public static Specification<MaintenanceWorkOrder>
    branchIn(Set<Long> branchIds) {

        return (root, query, cb) ->
                root.get("branchId")
                        .in(branchIds);
    }

    public static Specification<MaintenanceWorkOrder>
    equipmentEquals(Long equipmentId) {

        if (equipmentId == null) {
            return null;
        }

        return (root, query, cb) ->
                cb.equal(
                        root.get("equipmentId"),
                        equipmentId
                );
    }

    public static Specification<MaintenanceWorkOrder>
    statusEquals(WorkOrderStatus status) {

        if (status == null) {
            return null;
        }

        return (root, query, cb) ->
                cb.equal(
                        root.get("status"),
                        status
                );
    }

    public static Specification<MaintenanceWorkOrder>
    createdFrom(LocalDateTime from) {

        if (from == null) {
            return null;
        }

        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        from
                );
    }

    public static Specification<MaintenanceWorkOrder>
    createdTo(LocalDateTime to) {

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