package com.careerconnect.service;

import com.careerconnect.dto.CompanyRequest;
import com.careerconnect.dto.CompanyResponse;
import com.careerconnect.entity.Company;
import com.careerconnect.entity.User;
import com.careerconnect.exception.ResourceNotFoundException;
import com.careerconnect.repository.CompanyRepository;
import com.careerconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyService(CompanyRepository companyRepository, UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CompanyResponse getMine(Long ownerId) {
        Company company = companyRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("You have not created a company profile yet"));
        return CompanyResponse.from(company);
    }

    @Transactional
    public CompanyResponse saveMine(Long ownerId, CompanyRequest request) {
        Company company = companyRepository.findByOwnerId(ownerId).orElseGet(() -> {
            User owner = userRepository.findById(ownerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));
            Company created = new Company();
            created.setOwner(owner);
            return created;
        });

        company.setName(request.name().trim());
        company.setDescription(request.description() != null ? request.description().trim() : null);
        company.setWebsite(request.website());
        company.setLocation(request.location());
        return CompanyResponse.from(companyRepository.save(company));
    }
}