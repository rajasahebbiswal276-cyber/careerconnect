package com.careerconnect.dto;

import com.careerconnect.enums.ApplicationStatus;

import java.time.LocalDateTime;

public record TimelineEntry(ApplicationStatus status, String note, LocalDateTime changedAt) {
}
