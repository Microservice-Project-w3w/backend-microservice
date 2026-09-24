package com.equipmentrental.billing.security;

import com.equipmentrental.billing.dto.response.DebtResponse;
import com.equipmentrental.billing.dto.response.DepositResponse;
import com.equipmentrental.billing.dto.response.InvoiceResponse;
import com.equipmentrental.billing.dto.response.PaymentResponse;
import com.equipmentrental.billing.entity.Debt;
import com.equipmentrental.billing.entity.Deposit;
import com.equipmentrental.billing.entity.Invoice;
import com.equipmentrental.billing.entity.Payment;
import com.equipmentrental.billing.repository.DebtRepository;
import com.equipmentrental.billing.repository.DepositRepository;
import com.equipmentrental.billing.repository.InvoiceRepository;
import com.equipmentrental.billing.repository.PaymentRepository;
import com.equipmentrental.common.security.CurrentUser;
import com.equipmentrental.common.security.CurrentUserProvider;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component("billingScope")
public class BillingDataScopeGuard {

    private final CurrentUserProvider currentUserProvider;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final DepositRepository depositRepository;
    private final DebtRepository debtRepository;

    public BillingDataScopeGuard(
            CurrentUserProvider currentUserProvider,
            InvoiceRepository invoiceRepository,
            PaymentRepository paymentRepository,
            DepositRepository depositRepository,
            DebtRepository debtRepository) {
        this.currentUserProvider = currentUserProvider;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.depositRepository = depositRepository;
        this.debtRepository = debtRepository;
    }

    public boolean canAccess(Long organizationId, Long branchId, Long customerId) {
        CurrentUser user = currentUserProvider.getCurrentUser();
        if (isAdmin(user)) {
            return true;
        }
        if (organizationId == null || !organizationId.equals(user.organizationId())) {
            return false;
        }
        if (isCustomer(user)) {
            Long currentCustomerId = jwtCustomerId();
            return currentCustomerId != null && currentCustomerId.equals(customerId);
        }
        if (isAccountant(user)) {
            return true;
        }
        return branchId != null && user.branchIds().contains(branchId);
    }

    public boolean canAccessOrganization(Long organizationId) {
        CurrentUser user = currentUserProvider.getCurrentUser();
        return isAdmin(user)
                || (organizationId != null && organizationId.equals(user.organizationId()));
    }

    public boolean canAccessInvoice(Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .map(this::canAccess)
                .orElse(false);
    }

    public boolean canAccessPayment(Long paymentId) {
        return paymentRepository.findByIdWithInvoice(paymentId)
                .map(this::canAccess)
                .orElse(false);
    }

    public boolean canAccessDeposit(Long depositId) {
        return depositRepository.findById(depositId)
                .map(this::canAccess)
                .orElse(false);
    }

    public boolean canAccessDebt(Long debtId) {
        return debtRepository.findById(debtId)
                .map(this::canAccess)
                .orElse(false);
    }

    public boolean canAccessRentalOrder(Long rentalOrderId) {
        CurrentUser user = currentUserProvider.getCurrentUser();
        return isAdmin(user)
                || invoiceRepository.findByRentalOrderId(rentalOrderId)
                        .map(this::canAccess)
                        .orElse(false);
    }

    public boolean canAccessContract(Long contractId) {
        CurrentUser user = currentUserProvider.getCurrentUser();
        return isAdmin(user)
                || invoiceRepository.findByContractId(contractId)
                        .map(this::canAccess)
                        .orElse(false);
    }

    public boolean canAccessCustomer(Long customerId) {
        CurrentUser user = currentUserProvider.getCurrentUser();
        if (isAdmin(user)) {
            return true;
        }
        if (isCustomer(user)) {
            Long currentCustomerId = jwtCustomerId();
            return currentCustomerId != null && currentCustomerId.equals(customerId);
        }
        return invoiceRepository.findAll().stream()
                        .filter(invoice -> customerId.equals(invoice.getCustomerId()))
                        .anyMatch(this::canAccess)
                || debtRepository.findByCustomerId(customerId).stream().anyMatch(this::canAccess)
                || depositRepository.findAll().stream()
                        .filter(deposit -> customerId.equals(deposit.getCustomerId()))
                        .anyMatch(this::canAccess);
    }

    public List<InvoiceResponse> filterInvoices(List<InvoiceResponse> values) {
        return values.stream()
                .filter(value -> canAccess(
                        value.getOrganizationId(), value.getBranchId(), value.getCustomerId()))
                .toList();
    }

    public List<PaymentResponse> filterPayments(List<PaymentResponse> values) {
        return values.stream()
                .filter(value -> canAccess(
                        value.getOrganizationId(), value.getBranchId(), value.getCustomerId()))
                .toList();
    }

    public List<DepositResponse> filterDeposits(List<DepositResponse> values) {
        return values.stream()
                .filter(value -> canAccess(
                        value.getOrganizationId(), value.getBranchId(), value.getCustomerId()))
                .toList();
    }

    public List<DebtResponse> filterDebts(List<DebtResponse> values) {
        return values.stream()
                .filter(value -> canAccess(
                        value.getOrganizationId(), value.getBranchId(), value.getCustomerId()))
                .toList();
    }

    private boolean canAccess(Invoice invoice) {
        return canAccess(invoice.getOrganizationId(), invoice.getBranchId(), invoice.getCustomerId());
    }

    private boolean canAccess(Payment payment) {
        return payment.getInvoice() != null && canAccess(payment.getInvoice());
    }

    private boolean canAccess(Deposit deposit) {
        return canAccess(deposit.getOrganizationId(), deposit.getBranchId(), deposit.getCustomerId());
    }

    private boolean canAccess(Debt debt) {
        return canAccess(debt.getOrganizationId(), debt.getBranchId(), debt.getCustomerId());
    }

    private boolean isAdmin(CurrentUser user) {
        return hasRole(user, "ADMIN");
    }

    private boolean isAccountant(CurrentUser user) {
        return hasRole(user, "ACCOUNTANT");
    }

    private boolean isCustomer(CurrentUser user) {
        return hasRole(user, "CUSTOMER");
    }

    private boolean hasRole(CurrentUser user, String role) {
        return user.roles().contains(role) || user.roles().contains("ROLE_" + role);
    }

    private Long jwtCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }
        return CurrentUserProvider.toLong(jwt.getClaim("customerId"));
    }
}
