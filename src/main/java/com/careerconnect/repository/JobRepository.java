package com.careerconnect.repository;

import com.careerconnect.entity.Job;
import com.careerconnect.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    Page<Job> findByCompanyOwnerId(Long ownerId, Pageable pageable);

    long countByStatus(JobStatus status);
}
