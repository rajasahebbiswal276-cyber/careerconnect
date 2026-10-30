package com.careerconnect.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record ProfileRequest(
        @NotBlank(message = "Full name is required") String fullName,
        String phone,
        String headline,
        String location,
        String resumeUrl,
        @Min(value = 0, message = "Experience cannot be negative") Integer experienceYears,
        Set<String> skills) {
}
