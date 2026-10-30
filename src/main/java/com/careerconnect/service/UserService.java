package com.careerconnect.service;

import com.careerconnect.dto.ProfileRequest;
import com.careerconnect.dto.UserResponse;
import com.careerconnect.entity.User;
import com.careerconnect.exception.ResourceNotFoundException;
import com.careerconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SkillService skillService;

    public UserService(UserRepository userRepository, SkillService skillService) {
        this.userRepository = userRepository;
        this.skillService = skillService;
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        return UserResponse.from(find(userId));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, ProfileRequest request) {
        User user = find(userId);
        user.setFullName(request.fullName().trim());
        user.setPhone(request.phone());
        user.setHeadline(request.headline());
        user.setLocation(request.location());
        user.setResumeUrl(request.resumeUrl());
        user.setExperienceYears(request.experienceYears());
        if (request.skills() != null) {
            user.setSkills(skillService.resolve(request.skills()));
        }
        return UserResponse.from(userRepository.save(user));
    }

    public User find(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}