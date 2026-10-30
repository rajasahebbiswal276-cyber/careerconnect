package com.careerconnect.service;

import com.careerconnect.dto.AuthResponse;
import com.careerconnect.dto.LoginRequest;
import com.careerconnect.dto.RegisterRequest;
import com.careerconnect.entity.User;
import com.careerconnect.enums.Role;
import com.careerconnect.exception.BadRequestException;
import com.careerconnect.repository.UserRepository;
import com.careerconnect.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (request.role() == Role.ADMIN) {
            throw new BadRequestException("You can only register as CANDIDATE or RECRUITER");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this email already exists");
        }
        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
        User user = (User) auth.getPrincipal();
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }
}