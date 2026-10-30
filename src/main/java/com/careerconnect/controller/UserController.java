package com.careerconnect.controller;

import com.careerconnect.dto.ProfileRequest;
import com.careerconnect.dto.UserResponse;
import com.careerconnect.entity.User;
import com.careerconnect.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Profile")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal User user) {
        return userService.getProfile(user.getId());
    }

    @PutMapping("/me")
    public UserResponse update(@AuthenticationPrincipal User user, @Valid @RequestBody ProfileRequest request) {
        return userService.updateProfile(user.getId(), request);
    }
}