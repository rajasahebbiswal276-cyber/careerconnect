package com.careerconnect.controller;

import com.careerconnect.dto.CompanyRequest;
import com.careerconnect.dto.CompanyResponse;
import com.careerconnect.entity.User;
import com.careerconnect.service.CompanyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recruiter/company")
@PreAuthorize("hasRole('RECRUITER')")
@Tag(name = "Company")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public CompanyResponse get(@AuthenticationPrincipal User user) {
        return companyService.getMine(user.getId());
    }

    @PutMapping
    public CompanyResponse save(@AuthenticationPrincipal User user, @Valid @RequestBody CompanyRequest request) {
        return companyService.saveMine(user.getId(), request);
    }
}