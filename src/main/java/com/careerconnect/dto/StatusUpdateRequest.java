package com.careerconnect.dto;

import com.careerconnect.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StatusUpdateRequest(
        @NotNull(message = "Status is required") ApplicationStatus status,
        @Size(max = 1000, message = "Note is too long") String note) {
}
