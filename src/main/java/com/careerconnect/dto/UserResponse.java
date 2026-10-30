package com.careerconnect.dto;

import com.careerconnect.entity.Skill;
import com.careerconnect.entity.User;
import com.careerconnect.enums.Role;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(Long id, String fullName, String email, Role role, boolean enabled,
                           String phone, String headline, String location, String resumeUrl,
                           Integer experienceYears, List<String> skills, LocalDateTime createdAt) {

    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole(), u.isEnabled(),
                u.getPhone(), u.getHeadline(), u.getLocation(), u.getResumeUrl(), u.getExperienceYears(),
                u.getSkills().stream().map(Skill::getName).sorted().toList(), u.getCreatedAt());
    }
}
