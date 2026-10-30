package com.careerconnect.controller;

import com.careerconnect.dto.JobRequest;
import com.careerconnect.dto.JobResponse;
import com.careerconnect.dto.PageResponse;
import com.careerconnect.entity.User;
import com.careerconnect.enums.JobStatus;
import com.careerconnect.enums.JobType;
import com.careerconnect.service.JobService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api")
@Tag(name = "Jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    /** Public search with filters, pagination and sorting. */
    @GetMapping("/jobs")
    public PageResponse<JobResponse> search(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String location,
                                            @RequestParam(required = false) JobType jobType,
                                            @RequestParam(required = false) String skill,
                                            @RequestParam(required = false) BigDecimal minSalary,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(defaultValue = "postedAt") String sortBy,
                                            @RequestParam(defaultValue = "desc") String direction) {
        return jobService.search(keyword, location, jobType, skill, minSalary, page, size, sortBy, direction);
    }

    @GetMapping("/jobs/{id}")
    public JobResponse get(@PathVariable Long id) {
        return jobService.getById(id);
    }

    @PostMapping("/jobs")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RECRUITER')")
    public JobResponse create(@AuthenticationPrincipal User user, @Valid @RequestBody JobRequest request) {
        return jobService.create(user.getId(), request);
    }

    @PutMapping("/jobs/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public JobResponse update(@AuthenticationPrincipal User user, @PathVariable Long id,
                              @Valid @RequestBody JobRequest request) {
        return jobService.update(user.getId(), id, request);
    }

    @PatchMapping("/jobs/{id}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public JobResponse setStatus(@AuthenticationPrincipal User user, @PathVariable Long id,
                                 @RequestParam JobStatus status) {
        return jobService.setStatus(user.getId(), id, status);
    }

    @DeleteMapping("/jobs/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('RECRUITER')")
    public void delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        jobService.delete(user.getId(), id);
    }

    @GetMapping("/recruiter/jobs")
    @PreAuthorize("hasRole('RECRUITER')")
    public PageResponse<JobResponse> myJobs(@AuthenticationPrincipal User user,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return jobService.myJobs(user.getId(), page, size);
    }
}