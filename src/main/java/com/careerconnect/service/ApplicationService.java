package com.careerconnect.service;

import com.careerconnect.dto.ApplicationResponse;
import com.careerconnect.dto.ApplyRequest;
import com.careerconnect.dto.PageResponse;
import com.careerconnect.dto.StatusUpdateRequest;
import com.careerconnect.entity.Application;
import com.careerconnect.entity.Job;
import com.careerconnect.entity.User;
import com.careerconnect.enums.ApplicationStatus;
import com.careerconnect.enums.JobStatus;
import com.careerconnect.exception.BadRequestException;
import com.careerconnect.exception.ResourceNotFoundException;
import com.careerconnect.repository.ApplicationRepository;
import com.careerconnect.repository.JobRepository;
import com.careerconnect.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              JobRepository jobRepository,
                              UserRepository userRepository,
                              NotificationService notificationService) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public ApplicationResponse apply(Long candidateId, Long jobId, ApplyRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (job.getStatus() != JobStatus.OPEN) {
            throw new BadRequestException("This job is no longer accepting applications");
        }
        if (applicationRepository.existsByJobIdAndCandidateId(jobId, candidateId)) {
            throw new BadRequestException("You have already applied to this job");
        }
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Application application = new Application();
        application.setJob(job);
        application.setCandidate(candidate);
        application.setCoverLetter(request == null ? null : request.coverLetter());
        application.addHistory(ApplicationStatus.APPLIED, "Application submitted");
        return ApplicationResponse.from(applicationRepository.save(application));
    }

    public PageResponse<ApplicationResponse> myApplications(Long candidateId, int page, int size) {
        return PageResponse.of(applicationRepository.findByCandidateId(candidateId, pageable(page, size)),
                ApplicationResponse::from);
    }

    @Transactional
    public void withdraw(Long candidateId, Long applicationId) {
        Application application = find(applicationId);
        if (!application.getCandidate().getId().equals(candidateId)) {
            throw new AccessDeniedException("This is not your application");
        }
        if (application.getStatus() == ApplicationStatus.HIRED) {
            throw new BadRequestException("You cannot withdraw an application that has already been accepted");
        }
        applicationRepository.delete(application);
    }

    public PageResponse<ApplicationResponse> applicantsForJob(Long recruiterId, Long jobId,
                                                              ApplicationStatus status, int page, int size) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (!job.getCompany().getOwner().getId().equals(recruiterId)) {
            throw new AccessDeniedException("This job belongs to another recruiter");
        }
        var result = (status == null)
                ? applicationRepository.findByJobId(jobId, pageable(page, size))
                : applicationRepository.findByJobIdAndStatus(jobId, status, pageable(page, size));
        return PageResponse.of(result, ApplicationResponse::from);
    }

    @Transactional
    public ApplicationResponse updateStatus(Long recruiterId, Long applicationId, StatusUpdateRequest request) {
        Application application = find(applicationId);
        Job job = application.getJob();
        if (!job.getCompany().getOwner().getId().equals(recruiterId)) {
            throw new AccessDeniedException("This application belongs to another recruiter's job");
        }
        application.setStatus(request.status());
        application.setRecruiterNote(request.note());
        application.setUpdatedAt(LocalDateTime.now());
        application.addHistory(request.status(), request.note());
        Application saved = applicationRepository.save(application);

        User candidate = saved.getCandidate();
        notificationService.sendStatusUpdate(
                candidate.getEmail(),
                candidate.getFullName(),
                job.getTitle(),
                job.getCompany().getName(),
                request.status()
        );
        return ApplicationResponse.from(saved);
    }

    private Application find(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by(Sort.Direction.DESC, "appliedAt"));
    }
}