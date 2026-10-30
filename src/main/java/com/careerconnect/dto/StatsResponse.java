package com.careerconnect.dto;

public record StatsResponse(long totalUsers, long candidates, long recruiters, long totalJobs, long openJobs,
                            long totalApplications, long hired) {
}
