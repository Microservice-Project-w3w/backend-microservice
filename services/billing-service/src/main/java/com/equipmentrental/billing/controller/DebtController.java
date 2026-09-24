package com.equipmentrental.billing.controller;

import com.equipmentrental.billing.entity.Debt;
import com.equipmentrental.billing.service.DebtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/debts")
@RequiredArgsConstructor
public class DebtController {

    private final DebtService debtService;

    @PostMapping("/customers/{customerId}/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Debt> addDebt(@PathVariable Long customerId, @RequestParam BigDecimal amount) {
        Debt debt = debtService.addDebt(customerId, amount);
        return ResponseEntity.ok(debt);
    }

    @PostMapping("/customers/{customerId}/reduce")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Debt> reduceDebt(@PathVariable Long customerId, @RequestParam BigDecimal amount) {
        Debt debt = debtService.reduceDebt(customerId, amount);
        return ResponseEntity.ok(debt);
    }
}
