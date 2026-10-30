package com.careerconnect.repository;

import com.careerconnect.entity.Application;
import com.careerconnect.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
	boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);

	Page<Application> findByCandidateId(Long candidateId, Pageable pageable);

	Page<Application> findByCandidateIdAndStatus(Long candidateId, ApplicationStatus status, Pageable pageable);

	Page<Application> findByJobId(Long jobId, Pageable pageable);

	Page<Application> findByJobIdAndStatus(Long jobId, ApplicationStatus status, Pageable pageable);

	long countByStatus(ApplicationStatus status);
}
