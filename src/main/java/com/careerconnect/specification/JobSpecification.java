package com.careerconnect.specification;

import com.careerconnect.entity.Job;
import com.careerconnect.entity.Skill;
import com.careerconnect.enums.JobStatus;
import com.careerconnect.enums.JobType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Builds the dynamic WHERE clause for the public job search. */
public final class JobSpecification {

    private JobSpecification() {
    }

    public static Specification<Job> search(String keyword, String location, JobType jobType,
                                            String skill, BigDecimal minSalary) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            // Only open jobs from approved companies are public.
            predicates.add(cb.equal(root.get("status"), JobStatus.OPEN));
            predicates.add(cb.isTrue(root.get("company").<Boolean>get("approved")));

            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like),
                        cb.like(cb.lower(root.get("company").get("name")), like)));
            }
            if (location != null && !location.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.trim().toLowerCase() + "%"));
            }
            if (jobType != null) {
                predicates.add(cb.equal(root.get("jobType"), jobType));
            }
            if (skill != null && !skill.isBlank()) {
                Join<Job, Skill> skills = root.join("skills", JoinType.INNER);
                predicates.add(cb.equal(cb.lower(skills.get("name")), skill.trim().toLowerCase()));
            }
            if (minSalary != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.<BigDecimal>get("salaryMax"), minSalary));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
