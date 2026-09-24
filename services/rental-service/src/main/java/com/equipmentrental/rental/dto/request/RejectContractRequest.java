package com.equipmentrental.rental.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectContractRequest(@NotBlank @Size(max = 500) String reason) {}
