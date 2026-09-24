package com.equipmentrental.identity.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Fields that an authenticated user is allowed to edit on their own profile. */
public record UpdateProfileRequest(
        @NotBlank @Size(max = 150) String fullName,
        @Size(max = 30) String phone,
        @Size(max = 200) String companyName,
        @Size(max = 50) String taxCode) {}
