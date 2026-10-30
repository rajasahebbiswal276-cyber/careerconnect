package com.careerconnect.entity;

import com.careerconnect.enums.ApplicationStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "applications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"job_id", "candidate_id"}))
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id")
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id")
    private User candidate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(length = 2000)
    private String coverLetter;

    @Column(length = 1000)
    private String recruiterNote;

    private LocalDateTime appliedAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    /** Status timeline shown to both candidate and recruiter. */
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("changedAt ASC, id ASC")
    private List<StatusHistory> history = new ArrayList<>();

    public Application() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public User getCandidate() {
        return candidate;
    }

    public void setCandidate(User candidate) {
        this.candidate = candidate;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public String getRecruiterNote() {
        return recruiterNote;
    }

    public void setRecruiterNote(String recruiterNote) {
        this.recruiterNote = recruiterNote;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<StatusHistory> getHistory() {
        return history;
    }

    public void setHistory(List<StatusHistory> history) {
        this.history = history;
    }

    public void addHistory(ApplicationStatus newStatus, String note) {
        StatusHistory h = new StatusHistory();
        h.setApplication(this);
        h.setStatus(newStatus);
        h.setNote(note);
        h.setChangedAt(LocalDateTime.now());
        history.add(h);
    }
}