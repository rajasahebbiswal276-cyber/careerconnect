package com.careerconnect.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.careerconnect.entity.Application;
import com.careerconnect.entity.Company;
import com.careerconnect.entity.Job;
import com.careerconnect.entity.User;
import com.careerconnect.enums.ApplicationStatus;
import com.careerconnect.enums.JobType;
import com.careerconnect.enums.Role;
import com.careerconnect.repository.ApplicationRepository;
import com.careerconnect.repository.CompanyRepository;
import com.careerconnect.repository.JobRepository;
import com.careerconnect.repository.UserRepository;
import com.careerconnect.service.SkillService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final SkillService skillService;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      CompanyRepository companyRepository,
                      JobRepository jobRepository,
                      ApplicationRepository applicationRepository,
                      SkillService skillService,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.skillService = skillService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        log.info("Seeding initial data...");

        User admin = new User();
        admin.setFullName("Admin");
        admin.setEmail("admin@careerconnect.local");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        User recruiter = new User();
        recruiter.setFullName("Sarah Connor");
        recruiter.setEmail("recruiter@techcorp.io");
        recruiter.setPassword(passwordEncoder.encode("recruiter123"));
        recruiter.setRole(Role.RECRUITER);
        userRepository.save(recruiter);

        Company techcorp = new Company();
        techcorp.setName("TechCorp Solutions");
        techcorp.setDescription("Building the next generation of cloud developer tooling.");
        techcorp.setWebsite("https://techcorp.io");
        techcorp.setLocation("Bengaluru, India");
        techcorp.setOwner(recruiter);
        companyRepository.save(techcorp);

        User candidate = new User();
        candidate.setFullName("John Doe");
        candidate.setEmail("candidate@gmail.com");
        candidate.setPassword(passwordEncoder.encode("candidate123"));
        candidate.setRole(Role.CANDIDATE);
        candidate.setHeadline("Full-Stack Developer | Java & React");
        candidate.setLocation("Bengaluru, India");
        candidate.setExperienceYears(3);
        candidate.setSkills(skillService.resolve(Set.of("Java", "Spring Boot", "React", "PostgreSQL", "Docker")));
        userRepository.save(candidate);

        Job backend = new Job();
        backend.setTitle("Senior Java Backend Engineer");
        backend.setDescription("We are looking for an experienced Java developer with deep Spring Boot knowledge.");
        backend.setLocation("Bengaluru (Hybrid)");
        backend.setJobType(JobType.FULL_TIME);
        backend.setExperienceRequired(4);
        backend.setSalaryMin(BigDecimal.valueOf(1800000));
        backend.setSalaryMax(BigDecimal.valueOf(2800000));
        backend.setCompany(techcorp);
        backend.setSkills(skillService.resolve(Set.of("Java", "Spring Boot", "PostgreSQL", "Docker", "Microservices")));
        jobRepository.save(backend);

        Application app = new Application();
        app.setJob(backend);
        app.setCandidate(candidate);
        app.setCoverLetter("Excited about the work TechCorp is doing in cloud platforms.");
        app.addHistory(ApplicationStatus.APPLIED, "Initial application");
        applicationRepository.save(app);

        log.info("Seeding completed successfully.");
    }
}