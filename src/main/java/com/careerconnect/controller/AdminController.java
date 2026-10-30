package com.careerconnect.controller;

import com.careerconnect.dto.CompanyResponse;
import com.careerconnect.dto.PageResponse;
import com.careerconnect.dto.StatsResponse;
import com.careerconnect.dto.UserResponse;
import com.careerconnect.enums.Role;
import com.careerconnect.service.AdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public StatsResponse stats() {
        return adminService.stats();
    }

    @GetMapping("/users")
    public PageResponse<UserResponse> users(@RequestParam(required = false) Role role,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return adminService.users(role, page, size);
    }

    @PatchMapping("/users/{id}/toggle-status")
    public UserResponse toggleUser(@PathVariable Long id) {
        return adminService.toggleUserStatus(id);
    }

    @GetMapping("/companies")
    public PageResponse<CompanyResponse> companies(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return adminService.companies(page, size);
    }

    @PatchMapping("/companies/{id}/toggle-approval")
    public CompanyResponse toggleCompany(@PathVariable Long id) {
        return adminService.toggleCompanyApproval(id);
    }
}