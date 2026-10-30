package com.careerconnect.service;

import com.careerconnect.dto.CompanyResponse;
import com.careerconnect.dto.PageResponse;
import com.careerconnect.dto.StatsResponse;
import com.careerconnect.dto.UserResponse;
import com.careerconnect.entity.Company;
import com.careerconnect.entity.User;
import com.careerconnect.enums.ApplicationStatus;
import com.careerconnect.enums.JobStatus;
import com.careerconnect.enums.Role;
import com.careerconnect.exception.ResourceNotFoundException;
import com.careerconnect.repository.ApplicationRepository;
import com.careerconnect.repository.CompanyRepository;
import com.careerconnect.repository.JobRepository;
import com.careerconnect.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    public AdminService(UserRepository userRepository,
                        CompanyRepository companyRepository,
                        JobRepository jobRepository,
                        ApplicationRepository applicationRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    public StatsResponse stats() {
        return new StatsResponse(userRepository.count(),
                userRepository.countByRole(Role.CANDIDATE),
                userRepository.countByRole(Role.RECRUITER),
                jobRepository.count(),
                jobRepository.countByStatus(JobStatus.OPEN),
                applicationRepository.count(),
                applicationRepository.countByStatus(ApplicationStatus.HIRED));
    }

    public PageResponse<UserResponse> users(Role role, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        if (role != null) {
            return PageResponse.of(userRepository.findByRole(role, pageable), UserResponse::from);
        }
        return PageResponse.of(userRepository.findAll(pageable), UserResponse::from);
    }

    @Transactional
    public UserResponse toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEnabled(!user.isEnabled());
        return UserResponse.from(userRepository.save(user));
    }

    public PageResponse<CompanyResponse> companies(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.of(companyRepository.findAll(pageable), CompanyResponse::from);
    }

    @Transactional
    public CompanyResponse toggleCompanyApproval(Long companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        company.setApproved(!company.isApproved());
        return CompanyResponse.from(companyRepository.save(company));
    }
}