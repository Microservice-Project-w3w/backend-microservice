package com.equipmentrental.identity.dto.auth;

import java.util.List;

/** Keeps the existing /auth/me fields while supplying editable profile data. */
public record CurrentUserProfileResponse(
        Long userId,
        String fullName,
        String email,
        String phone,
        String companyName,
        String taxCode,
        List<String> roles) {}
