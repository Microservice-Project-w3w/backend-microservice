package com.equipmentrental.billing.controller;

import com.equipmentrental.billing.dto.request.InternalCreateInvoiceRequest;
import com.equipmentrental.billing.dto.response.InvoiceResponse;
import com.equipmentrental.billing.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.equipmentrental.billing.dto.request.InternalAddChargeRequest;
import com.equipmentrental.billing.dto.response.InternalRentalOrderBillingStatusResponse;
import com.equipmentrental.billing.dto.response.InternalContractSettlementStatusResponse;
import com.equipmentrental.billing.dto.response.InternalCustomerDebtStatusResponse;
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalBillingController {

    private final BillingService billingService;

    @PostMapping("/invoices")
    @PreAuthorize("hasAuthority('billing.invoice.create') and @billingScope.canAccess(#request.organizationId, #request.branchId, #request.customerId)")
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse createInternalInvoice(
            @Valid @RequestBody InternalCreateInvoiceRequest request
    ) {
        return billingService.createInternalInvoice(request);
    }
    @PostMapping("/invoices/{id}/charges")
    @PreAuthorize("hasAuthority('billing.invoice.update') and @billingScope.canAccessInvoice(#id)")
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse addInternalCharge(
            @PathVariable Long id,
            @Valid @RequestBody InternalAddChargeRequest request
    ) {
        return billingService.addInternalCharge(
                id,
                request
        );
    }
    @GetMapping("/rental-orders/{rentalOrderId}/billing-status")
    @PreAuthorize("hasAuthority('billing.invoice.read') and @billingScope.canAccessRentalOrder(#rentalOrderId)")
    public InternalRentalOrderBillingStatusResponse getRentalOrderBillingStatus(
            @PathVariable Long rentalOrderId
    ) {
        return billingService.getInternalRentalOrderBillingStatus(
                rentalOrderId
        );
    }

    @GetMapping("/rental-contracts/{contractId}/settlement-status")
    @PreAuthorize("hasAuthority('billing.invoice.read') and @billingScope.canAccessContract(#contractId)")
    public InternalContractSettlementStatusResponse getContractSettlementStatus(
            @PathVariable Long contractId
    ) {
        return billingService.getInternalContractSettlementStatus(contractId);
    }

    @GetMapping("/customers/{customerId}/debt-status")
    @PreAuthorize("hasAuthority('billing.debt.read') and @billingScope.canAccessCustomer(#customerId)")
    public InternalCustomerDebtStatusResponse getCustomerDebtStatus(
            @PathVariable Long customerId
    ) {
        return billingService.getInternalCustomerDebtStatus(customerId);
    }
}
