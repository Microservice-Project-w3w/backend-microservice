package com.equipmentrental.maintenance.controller;

import com.equipmentrental.maintenance.dto.request.CreateCustomerIssueRequest;
import com.equipmentrental.maintenance.dto.response.CustomerIssueResponse;
import com.equipmentrental.maintenance.service.CustomerIssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/v1/maintenance/customer/issues"
)
@RequiredArgsConstructor
public class CustomerIssueController {

    private final CustomerIssueService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(
            "hasRole('CUSTOMER')"
    )
    public CustomerIssueResponse create(
            @Valid
            @RequestBody
            CreateCustomerIssueRequest request
    ) {

        return service.create(request);
    }

    @GetMapping
    @PreAuthorize(
            "hasRole('CUSTOMER')"
    )
    public List<CustomerIssueResponse> list() {

        return service.listOwn();
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasRole('CUSTOMER')"
    )
    public CustomerIssueResponse detail(
            @PathVariable Long id
    ) {

        return service.getOwnById(id);
    }
}