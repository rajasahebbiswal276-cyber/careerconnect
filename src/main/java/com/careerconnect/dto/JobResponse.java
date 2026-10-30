package com.careerconnect.dto;

import com.careerconnect.entity.Job;
import com.careerconnect.entity.Skill;
import com.careerconnect.enums.JobStatus;
import com.careerconnect.enums.JobType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record JobResponse(Long id, String title, String description, String location, JobType jobType,
                          Integer experienceRequired, BigDecimal salaryMin, BigDecimal salaryMax,
                          JobStatus status, LocalDateTime postedAt, Long companyId, String companyName,
                          List<String> skills) {

    public static JobResponse from(Job j) {
        return new JobResponse(j.getId(), j.getTitle(), j.getDescription(), j.getLocation(), j.getJobType(),
                j.getExperienceRequired(), j.getSalaryMin(), j.getSalaryMax(), j.getStatus(), j.getPostedAt(),
                j.getCompany().getId(), j.getCompany().getName(),
                j.getSkills().stream().map(Skill::getName).sorted().toList());
    }
}
