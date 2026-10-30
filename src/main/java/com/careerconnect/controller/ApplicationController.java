package com.careerconnect.controller;

import com.careerconnect.dto.ApplicationResponse;
import com.careerconnect.dto.ApplyRequest;
import com.careerconnect.dto.PageResponse;
import com.careerconnect.dto.StatusUpdateRequest;
import com.careerconnect.entity.User;
import com.careerconnect.enums.ApplicationStatus;
import com.careerconnect.service.ApplicationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/jobs/{jobId}/apply")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApplicationResponse apply(@AuthenticationPrincipal User user,
                                     @PathVariable Long jobId,
                                     @Valid @RequestBody(required = false) ApplyRequest request) {
        return applicationService.apply(user.getId(), jobId, request);
    }

    @GetMapping("/applications/my")
    @PreAuthorize("hasRole('CANDIDATE')")
    public PageResponse<ApplicationResponse> mine(@AuthenticationPrincipal User user,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return applicationService.myApplications(user.getId(), page, size);
    }

    @DeleteMapping("/applications/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('CANDIDATE')")
    public void withdraw(@AuthenticationPrincipal User user, @PathVariable Long id) {
        applicationService.withdraw(user.getId(), id);
    }

    @GetMapping("/recruiter/jobs/{jobId}/applications")
    @PreAuthorize("hasRole('RECRUITER')")
    public PageResponse<ApplicationResponse> applicants(@AuthenticationPrincipal User user,
                                                        @PathVariable Long jobId,
                                                        @RequestParam(required = false) ApplicationStatus status,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        return applicationService.applicantsForJob(user.getId(), jobId, status, page, size);
    }

    @PatchMapping("/applications/{id}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApplicationResponse updateStatus(@AuthenticationPrincipal User user,
                                            @PathVariable Long id,
                                            @Valid @RequestBody StatusUpdateRequest request) {
        return applicationService.updateStatus(user.getId(), id, request);
    }
}