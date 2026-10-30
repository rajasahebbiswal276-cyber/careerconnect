package com.careerconnect;

import com.careerconnect.dto.ApplyRequest;
import com.careerconnect.entity.Application;
import com.careerconnect.entity.Company;
import com.careerconnect.entity.Job;
import com.careerconnect.entity.User;
import com.careerconnect.enums.ApplicationStatus;
import com.careerconnect.enums.JobStatus;
import com.careerconnect.exception.BadRequestException;
import com.careerconnect.repository.ApplicationRepository;
import com.careerconnect.repository.JobRepository;
import com.careerconnect.repository.UserRepository;
import com.careerconnect.service.ApplicationService;
import com.careerconnect.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock ApplicationRepository applicationRepository;
    @Mock JobRepository jobRepository;
    @Mock UserRepository userRepository;
    @Mock NotificationService notificationService;
    @InjectMocks ApplicationService service;

    @Test
    void applyingTwiceToTheSameJobIsRejected() {
        Job job = new Job();
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByJobIdAndCandidateId(1L, 7L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.apply(7L, 1L, new ApplyRequest("Hi")));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void cannotApplyToAClosedJob() {
        Job job = new Job();
        job.setStatus(JobStatus.CLOSED);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        assertThrows(BadRequestException.class, () -> service.apply(7L, 1L, new ApplyRequest("Hi")));
    }

    @Test
    void recruiterCannotChangeStatusOfAnotherRecruitersApplication() {
        User owner = new User();
        owner.setId(100L);
        Company company = new Company();
        company.setOwner(owner);
        Job job = new Job();
        job.setCompany(company);
        Application application = new Application();
        application.setJob(job);
        when(applicationRepository.findById(5L)).thenReturn(Optional.of(application));

        var request = new com.careerconnect.dto.StatusUpdateRequest(ApplicationStatus.HIRED, "welcome");
        assertThrows(AccessDeniedException.class, () -> service.updateStatus(999L, 5L, request));
        verify(notificationService, never()).sendStatusUpdate(any(), any(), any(), any(), any());
    }
}
