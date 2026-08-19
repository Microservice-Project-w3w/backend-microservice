package com.equipmentrental.inventory.security;

import com.equipmentrental.common.security.CurrentUser;
import com.equipmentrental.common.security.CurrentUserProvider;
import com.equipmentrental.common.security.DataScopeAuthorizer;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Component
public class InventoryDataScopeGuard {

    private final CurrentUserProvider currentUserProvider;
    private final DataScopeAuthorizer dataScopeAuthorizer;

    public InventoryDataScopeGuard(
            CurrentUserProvider currentUserProvider,
            DataScopeAuthorizer dataScopeAuthorizer
    ) {
        this.currentUserProvider = currentUserProvider;
        this.dataScopeAuthorizer = dataScopeAuthorizer;
    }

    
    public CurrentUser getCurrentUser() {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();

        if (currentUser == null
                || currentUser.userId() == null
                || currentUser.userId().isBlank()) {

            throw new AccessDeniedException(
                    "Không xác định được người dùng hiện tại"
            );
        }

        return currentUser;
    }

   
    public void checkOrganization(Long organizationId) {

        if (organizationId == null) {
            throw new AccessDeniedException(
                    "organizationId không được để trống"
            );
        }

        CurrentUser currentUser = getCurrentUser();

        boolean allowed =
                dataScopeAuthorizer.canAccessOrganization(
                        currentUser,
                        organizationId
                );

        if (!allowed) {
            throw new AccessDeniedException(
                    "Bạn không có quyền truy cập organizationId="
                            + organizationId
            );
        }
    }

   
    public void checkBranch(
            Long organizationId,
            Long branchId
    ) {

        if (organizationId == null) {
            throw new AccessDeniedException(
                    "organizationId không được để trống"
            );
        }

        if (branchId == null) {
            throw new AccessDeniedException(
                    "branchId không được để trống"
            );
        }

        CurrentUser currentUser = getCurrentUser();

        boolean allowed =
                dataScopeAuthorizer.canAccessBranch(
                        currentUser,
                        organizationId,
                        branchId
                );

        if (!allowed) {
            throw new AccessDeniedException(
                    "Bạn không có quyền truy cập organizationId="
                            + organizationId
                            + ", branchId="
                            + branchId
            );
        }
    }

    /**
     * Read-only catalog access for CUSTOMER is organization-wide because equipment is
     * not owned by a customer. All employee roles remain restricted to assigned branches.
     */
    public void checkReadableBranch(Long organizationId, Long branchId) {
        checkOrganization(organizationId);
        if (!isCustomer()) {
            checkBranch(organizationId, branchId);
        }
    }

    public <T> List<T> filterAssignedBranches(
            Long organizationId,
            List<T> values,
            Function<T, Long> branchIdExtractor
    ) {
        checkOrganization(organizationId);
        if (isAdmin()) {
            return values;
        }

        Set<Long> branchIds = getCurrentBranchIds();
        return values.stream()
                .filter(value -> branchIds.contains(branchIdExtractor.apply(value)))
                .toList();
    }

    public <T> List<T> filterReadableBranches(
            Long organizationId,
            List<T> values,
            Function<T, Long> branchIdExtractor
    ) {
        checkOrganization(organizationId);
        if (isAdmin() || isCustomer()) {
            return values;
        }
        return filterAssignedBranches(organizationId, values, branchIdExtractor);
    }

   
    public void checkOwner(String ownerUserId) {

        if (ownerUserId == null || ownerUserId.isBlank()) {
            throw new AccessDeniedException(
                    "ownerUserId không được để trống"
            );
        }

        CurrentUser currentUser = getCurrentUser();

        boolean allowed =
                dataScopeAuthorizer.canAccessOwner(
                        currentUser,
                        ownerUserId
                );

        if (!allowed) {
            throw new AccessDeniedException(
                    "Bạn không có quyền truy cập dữ liệu của userId="
                            + ownerUserId
            );
        }
    }

    public String getCurrentUserId() {
        return getCurrentUser().userId();
    }

 
    public Long getCurrentOrganizationId() {
        return getCurrentUser().organizationId();
    }

  
    public Set<Long> getCurrentBranchIds() {
        return getCurrentUser().branchIds();
    }


    public boolean isAdmin() {

        CurrentUser currentUser = getCurrentUser();

        return currentUser.roles().contains("ADMIN")
                || currentUser.roles().contains("ROLE_ADMIN");
    }

    public boolean isCustomer() {
        CurrentUser currentUser = getCurrentUser();
        return currentUser.roles().contains("CUSTOMER")
                || currentUser.roles().contains("ROLE_CUSTOMER");
    }

    public boolean canAccessBranch(
            Long organizationId,
            Long branchId
    ) {

        if (organizationId == null || branchId == null) {
            return false;
        }

        CurrentUser currentUser = getCurrentUser();

        return dataScopeAuthorizer.canAccessBranch(
                currentUser,
                organizationId,
                branchId
        );
    }

    
    public boolean canAccessOrganization(Long organizationId) {

        if (organizationId == null) {
            return false;
        }

        CurrentUser currentUser = getCurrentUser();

        return dataScopeAuthorizer.canAccessOrganization(
                currentUser,
                organizationId
        );
    }
}
