package com.equipmentrental.billing.controller;

import com.equipmentrental.billing.dto.request.CreateDepositRequest;
import com.equipmentrental.billing.dto.response.DepositResponse;
import com.equipmentrental.billing.service.DepositService;
import com.equipmentrental.billing.security.BillingDataScopeGuard;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import com.equipmentrental.billing.dto.request.CreateDepositDeductionRequest;
import com.equipmentrental.billing.dto.response.DepositDeductionResponse;
import com.equipmentrental.billing.dto.request.RefundDepositRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/v1/billing/deposits")
@RequiredArgsConstructor
public class DepositController {

    private final DepositService depositService;
    private final BillingDataScopeGuard billingScope;

    @PostMapping
    @PreAuthorize("hasAuthority('billing.deposit.collect') and @billingScope.canAccess(#request.organizationId, #request.branchId, #request.customerId)")
    @ResponseStatus(HttpStatus.CREATED)
    public DepositResponse createDeposit(
            @Valid @RequestBody CreateDepositRequest request
    ) {
        return depositService.createDeposit(request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('billing.deposit.read')")
    public List<DepositResponse> getAllDeposits() {
        return billingScope.filterDeposits(depositService.getAllDeposits());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('billing.deposit.read') and @billingScope.canAccessDeposit(#id)")
    public DepositResponse getDepositById(
            @PathVariable Long id
    ) {
        return depositService.getDepositById(id);
    }

    @PostMapping("/{id}/deductions")
    @PreAuthorize("hasAuthority('billing.deposit.deduct') and @billingScope.canAccessDeposit(#id)")
    public DepositResponse deductDeposit(
            @PathVariable Long id,
            @Valid @RequestBody CreateDepositDeductionRequest request
    ) {
        return depositService.deductDeposit(id, request);
    }
    @GetMapping("/{id}/deductions")
    @PreAuthorize("hasAuthority('billing.deposit.read') and @billingScope.canAccessDeposit(#id)")
    public List<DepositDeductionResponse> getDeductions(
            @PathVariable Long id
    ) {
        return depositService.getDeductions(id);
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAuthority('billing.deposit.refund') and @billingScope.canAccessDeposit(#id)")
    public DepositResponse refundDeposit(
            @PathVariable Long id,
            @Valid @RequestBody RefundDepositRequest request
    ) {
        return depositService.refundDeposit(id, request);
    }

}
