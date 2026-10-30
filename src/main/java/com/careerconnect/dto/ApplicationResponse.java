package com.careerconnect.dto;

import com.careerconnect.entity.Application;
import com.careerconnect.entity.Job;
import com.careerconnect.entity.Skill;
import com.careerconnect.entity.User;
import com.careerconnect.enums.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record ApplicationResponse(Long id, Long jobId, String jobTitle, String companyName,
                                  Long candidateId, String candidateName, String candidateEmail,
                                  String headline, String resumeUrl, Integer experienceYears,
                                  List<String> candidateSkills, int skillMatchPercent,
                                  ApplicationStatus status, String coverLetter, String recruiterNote,
                                  LocalDateTime appliedAt, LocalDateTime updatedAt,
                                  List<TimelineEntry> timeline) {

    public static ApplicationResponse from(Application a) {
        Job j = a.getJob();
        User c = a.getCandidate();
        Set<String> jobSkills = lower(j.getSkills());
        Set<String> candSkills = lower(c.getSkills());
        int match = jobSkills.isEmpty() ? 0
                : (int) Math.round(100.0 * jobSkills.stream().filter(candSkills::contains).count() / jobSkills.size());
        return new ApplicationResponse(a.getId(), j.getId(), j.getTitle(), j.getCompany().getName(),
                c.getId(), c.getFullName(), c.getEmail(), c.getHeadline(), c.getResumeUrl(), c.getExperienceYears(),
                c.getSkills().stream().map(Skill::getName).sorted().toList(), match,
                a.getStatus(), a.getCoverLetter(), a.getRecruiterNote(), a.getAppliedAt(), a.getUpdatedAt(),
                a.getHistory().stream().map(h -> new TimelineEntry(h.getStatus(), h.getNote(), h.getChangedAt())).toList());
    }

    private static Set<String> lower(Set<Skill> skills) {
        return skills.stream().map(s -> s.getName().toLowerCase()).collect(Collectors.toSet());
    }

	public Long id() {
		return id;
	}

	public Long jobId() {
		return jobId;
	}

	public String jobTitle() {
		return jobTitle;
	}

	public String companyName() {
		return companyName;
	}

	public Long candidateId() {
		return candidateId;
	}

	public String candidateName() {
		return candidateName;
	}

	public String candidateEmail() {
		return candidateEmail;
	}

	public String headline() {
		return headline;
	}

	public String resumeUrl() {
		return resumeUrl;
	}

	public Integer experienceYears() {
		return experienceYears;
	}

	public List<String> candidateSkills() {
		return candidateSkills;
	}

	public int skillMatchPercent() {
		return skillMatchPercent;
	}

	public ApplicationStatus status() {
		return status;
	}

	public String coverLetter() {
		return coverLetter;
	}

	public String recruiterNote() {
		return recruiterNote;
	}

	public LocalDateTime appliedAt() {
		return appliedAt;
	}

	public LocalDateTime updatedAt() {
		return updatedAt;
	}

	public List<TimelineEntry> timeline() {
		return timeline;
	}
    
}
