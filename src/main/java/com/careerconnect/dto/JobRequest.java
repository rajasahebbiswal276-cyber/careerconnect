package com.careerconnect.dto;

import com.careerconnect.enums.JobType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;

public record JobRequest(
        @NotBlank(message = "Title is required") String title,
        @NotBlank(message = "Description is required") @Size(max = 4000, message = "Description is too long") String description,
        @NotBlank(message = "Location is required") String location,
        @NotNull(message = "Job type is required") JobType jobType,
        @Min(value = 0, message = "Experience cannot be negative") Integer experienceRequired,
        @PositiveOrZero(message = "Minimum salary cannot be negative") BigDecimal salaryMin,
        @PositiveOrZero(message = "Maximum salary cannot be negative") BigDecimal salaryMax,
        Set<String> skills) {
}
