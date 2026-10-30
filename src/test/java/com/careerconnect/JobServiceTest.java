package com.careerconnect;

import com.careerconnect.dto.JobRequest;
import com.careerconnect.entity.Company;
import com.careerconnect.enums.JobType;
import com.careerconnect.exception.BadRequestException;
import com.careerconnect.repository.CompanyRepository;
import com.careerconnect.repository.JobRepository;
import com.careerconnect.service.JobService;
import com.careerconnect.service.SkillService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock JobRepository jobRepository;
    @Mock CompanyRepository companyRepository;
    @Mock SkillService skillService;
    @InjectMocks JobService service;

    private JobRequest request(BigDecimal min, BigDecimal max) {
        return new JobRequest("Java Developer", "Build APIs", "Delhi", JobType.FULL_TIME, 1, min, max, Set.of("Java"));
    }

    @Test
    void recruiterWithoutCompanyCannotPostJobs() {
        when(companyRepository.findByOwnerId(1L)).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class,
                () -> service.create(1L, request(BigDecimal.ONE, BigDecimal.TEN)));
    }

    @Test
    void minimumSalaryCannotExceedMaximum() {
        when(companyRepository.findByOwnerId(1L)).thenReturn(Optional.of(new Company()));
        assertThrows(BadRequestException.class,
                () -> service.create(1L, request(BigDecimal.TEN, BigDecimal.ONE)));
    }
}
