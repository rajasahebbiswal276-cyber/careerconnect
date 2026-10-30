package com.careerconnect.service;

import com.careerconnect.dto.JobRequest;
import com.careerconnect.dto.JobResponse;
import com.careerconnect.dto.PageResponse;
import com.careerconnect.entity.Company;
import com.careerconnect.entity.Job;
import com.careerconnect.enums.JobStatus;
import com.careerconnect.enums.JobType;
import com.careerconnect.exception.BadRequestException;
import com.careerconnect.exception.ResourceNotFoundException;
import com.careerconnect.repository.CompanyRepository;
import com.careerconnect.repository.JobRepository;
import com.careerconnect.specification.JobSpecification;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class JobService {

    private static final Set<String> SORTABLE = Set.of("postedAt", "salaryMax", "title");

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final SkillService skillService;

    public JobService(JobRepository jobRepository,
                      CompanyRepository companyRepository,
                      SkillService skillService) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.skillService = skillService;
    }

    public PageResponse<JobResponse> search(String keyword, String location, JobType jobType, String skill,
                                            BigDecimal minSalary, int page, int size, String sortBy, String direction) {
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String field = SORTABLE.contains(sortBy) ? sortBy : "postedAt";
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(dir, field));
        return PageResponse.of(
                jobRepository.findAll(JobSpecification.search(keyword, location, jobType, skill, minSalary), pageable),
                JobResponse::from);
    }

    public JobResponse get(Long id) {
        return JobResponse.from(find(id));
    }

    public JobResponse getById(Long id) {
        return get(id);
    }

    public PageResponse<JobResponse> myJobs(Long recruiterId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50),
                Sort.by(Sort.Direction.DESC, "postedAt"));
        return PageResponse.of(jobRepository.findByCompanyOwnerId(recruiterId, pageable), JobResponse::from);
    }

    @Transactional
    public JobResponse create(Long recruiterId, JobRequest request) {
        Company company = companyRepository.findByOwnerId(recruiterId)
                .orElseThrow(() -> new BadRequestException("Create your company profile before posting jobs"));
        if (!company.isApproved()) {
            throw new AccessDeniedException("Your company is not approved");
        }
        Job job = new Job();
        job.setCompany(company);
        apply(job, request);
        return JobResponse.from(jobRepository.save(job));
    }

    @Transactional
    public JobResponse update(Long recruiterId, Long jobId, JobRequest request) {
        Job job = ownedJob(recruiterId, jobId);
        apply(job, request);
        return JobResponse.from(jobRepository.save(job));
    }

    @Transactional
    public JobResponse setStatus(Long recruiterId, Long jobId, JobStatus status) {
        Job job = ownedJob(recruiterId, jobId);
        job.setStatus(status);
        return JobResponse.from(jobRepository.save(job));
    }

    @Transactional
    public void delete(Long recruiterId, Long jobId) {
        jobRepository.delete(ownedJob(recruiterId, jobId));
    }

    private void apply(Job job, JobRequest r) {
        if (r.salaryMin() != null && r.salaryMax() != null && r.salaryMin().compareTo(r.salaryMax()) > 0) {
            throw new BadRequestException("Minimum salary cannot be greater than maximum salary");
        }
        job.setTitle(r.title().trim());
        job.setDescription(r.description().trim());
        job.setLocation(r.location().trim());
        job.setJobType(r.jobType());
        job.setExperienceRequired(r.experienceRequired());
        job.setSalaryMin(r.salaryMin());
        job.setSalaryMax(r.salaryMax());
        job.setSkills(skillService.resolve(r.skills()));
    }

    private Job find(Long id) {
        return jobRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    private Job ownedJob(Long recruiterId, Long jobId) {
        Job job = find(jobId);
        if (!job.getCompany().getOwner().getId().equals(recruiterId)) {
            throw new AccessDeniedException("This job belongs to another recruiter");
        }
        return job;
    }
}