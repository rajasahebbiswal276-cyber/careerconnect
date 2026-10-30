# CareerConnect: Job Portal & Recruitment Management System

A full stack job portal built with **Spring Boot 3, Spring Security (JWT), Spring Data JPA, MySQL/H2** and a lightweight JavaScript frontend served by the same application.

Recruiters post jobs and manage applicants, candidates search and apply, and admins moderate the platform.

## Highlights

- JWT authentication with BCrypt password hashing and **role-based access** (`ADMIN`, `RECRUITER`, `CANDIDATE`)
- Job search with **dynamic filters** (keyword, location, type, skill, minimum salary), **pagination and sorting** using JPA Specifications
- **Application tracking timeline**: every status change is recorded and the candidate gets an **email notification**
- **Skill match %** between a candidate's skills and a job's required skills, shown to recruiters
- Admin dashboard: platform statistics, block/unblock users, approve or hide companies, remove jobs
- Layered architecture (Controller, Service, Repository), DTOs (Java records), Bean Validation, **global exception handling**
- Swagger/OpenAPI docs, unit tests (JUnit 5 + Mockito), integration tests (MockMvc), Dockerfile and docker-compose

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.2, Spring MVC, Spring Security, Spring Data JPA, Spring Mail, Bean Validation |
| Auth | JWT (jjwt), BCrypt |
| Database | H2 in-memory (default), MySQL 8 (profile `mysql`) |
| Frontend | HTML, CSS, vanilla JavaScript (single page app in `src/main/resources/static`) |
| Docs and tests | springdoc OpenAPI (Swagger UI), JUnit 5, Mockito, MockMvc |
| DevOps | Maven, Docker, docker-compose, Git |

## Run it

Requirements: **JDK 17+** and **Maven 3.9+** (or open the folder in IntelliJ / Eclipse / VS Code as a Maven project).

```bash
mvn spring-boot:run
```

Open **http://localhost:8080**. The app starts with demo data, so you can log in immediately:

| Role | Email | Password |
|---|---|---|
| Candidate | candidate@demo.com | Candidate@123 |
| Recruiter | recruiter@demo.com | Recruiter@123 |
| Admin | admin@careerconnect.com | Admin@123 |

Other useful URLs:

- Swagger UI: http://localhost:8080/swagger-ui.html (click **Authorize** and paste the token from `/api/auth/login`)
- H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:careerconnect`, user `sa`, empty password)

### Use MySQL instead of H2

```bash
# MySQL must be running; the database is created automatically
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
# optional overrides: DB_URL, DB_USERNAME, DB_PASSWORD
```

### Docker

```bash
docker compose up --build        # app on :8080 and MySQL on :3306
```

### Tests

```bash
mvn test
```

### Real emails (optional)

Uncomment the `spring.mail.*` lines in `application.properties` (for Gmail use an app password). Without them, notifications are written to the log instead.

## Try the main flow (5 minutes)

1. Log in as **candidate**, search "java", open a job and click **Apply now**.
2. Log in as **recruiter**, open **My jobs**, then **Applicants**. You will see the skill match score. Change the status to *Shortlisted* with a note.
3. Log in again as **candidate** and open **My applications** to see the timeline update. The email text appears in the server log.
4. Log in as **admin**, open the dashboard and block a user or hide a company. Hidden companies disappear from public search.

## Project structure

```
src/main/java/com/careerconnect
├── config/          SecurityConfig, OpenApiConfig, DataSeeder
├── controller/      Auth, User, Company, Job, Application, Admin REST controllers
├── dto/             Request and response records (never expose entities)
├── entity/          User, Company, Job, Skill, Application, StatusHistory
├── enums/           Role, JobType, JobStatus, ApplicationStatus
├── exception/       Custom exceptions and GlobalExceptionHandler
├── repository/      Spring Data JPA repositories
├── security/        JwtService, JwtAuthFilter, CustomUserDetailsService
├── service/         Business logic (Auth, Job, Application, Admin, Notification...)
└── specification/   JobSpecification (dynamic search query)
```

## Database design

```
users ──< company (owner)            company ──< job
users ──< applications >── job       applications ──< status_history
job >──< skill  (job_skills)         users >──< skill (candidate_skills)
```

- One-to-many: Company to Jobs, Job to Applications, Application to StatusHistory
- Many-to-many: Job and Skill, User and Skill
- Unique constraint on (job_id, candidate_id) so a candidate cannot apply twice

## REST API summary

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | Public |
| GET | `/api/jobs?keyword=&location=&jobType=&skill=&minSalary=&page=&size=&sortBy=&direction=` | Public |
| GET | `/api/jobs/{id}` | Public |
| POST / PUT / DELETE | `/api/jobs`, `/api/jobs/{id}` | Recruiter (owner only) |
| PATCH | `/api/jobs/{id}/status?status=OPEN\|CLOSED` | Recruiter (owner only) |
| GET / PUT | `/api/recruiter/company` | Recruiter |
| GET | `/api/recruiter/jobs` | Recruiter |
| GET | `/api/recruiter/jobs/{jobId}/applications?status=` | Recruiter (owner only) |
| PATCH | `/api/applications/{id}/status` | Recruiter (owner only) |
| POST | `/api/jobs/{jobId}/apply` | Candidate |
| GET / DELETE | `/api/applications/my`, `/api/applications/{id}` | Candidate |
| GET / PUT | `/api/users/me` | Any logged-in user |
| GET | `/api/admin/stats`, `/api/admin/users`, `/api/admin/companies` | Admin |
| PATCH | `/api/admin/users/{id}/status?enabled=`, `/api/admin/companies/{id}/approval?approved=` | Admin |
| DELETE | `/api/admin/jobs/{id}` | Admin |

Errors always come back as JSON: `{ timestamp, status, error, message, fieldErrors }`.

## Security notes

- Passwords are hashed with BCrypt. Nobody can self-register as `ADMIN`.
- Set a strong `JWT_SECRET` (Base64, at least 32 bytes) in production instead of the default in `application.properties`.
- Recruiters can only modify their own jobs and applicants. Blocked users are rejected on their very next request.

## Resume bullet points

- Developed **CareerConnect**, a full stack job portal with Spring Boot, Spring Security (JWT), Spring Data JPA and MySQL, supporting three roles (Admin, Recruiter, Candidate) with role-based access control.
- Built 30+ REST APIs with DTOs, Bean Validation and a global exception handler; documented with Swagger/OpenAPI.
- Implemented dynamic job search (keyword, location, skill, salary) with pagination and sorting using JPA Specifications.
- Designed an application tracking timeline with automatic email notifications and a skill-match score to help recruiters shortlist faster.
- Wrote unit tests (JUnit 5, Mockito) and MockMvc integration tests; containerised the app with Docker and docker-compose.

## Interview questions you should be ready for

1. **How does JWT authentication work in your project?** The login endpoint verifies credentials through `AuthenticationManager` and returns a signed token. `JwtAuthFilter` reads the `Authorization: Bearer` header on each request, validates the token, loads the user and sets the `SecurityContext`. The API is stateless.
2. **Why DTOs instead of returning entities?** To avoid exposing fields like the password hash, to avoid lazy-loading and infinite recursion problems, and to keep the API stable when the schema changes.
3. **How do you stop a candidate applying twice?** A service check (`existsByJobIdAndCandidateId`) gives a friendly error, and a database unique constraint on (job_id, candidate_id) guarantees it even under concurrent requests.
4. **How does the dynamic search work?** `JobSpecification` builds a JPA Criteria `Predicate` list depending on which filters were sent, and `JpaSpecificationExecutor` runs it with a `Pageable`.
5. **How is authorization enforced?** URL rules in `SecurityConfig`, `@PreAuthorize` on controller methods, and ownership checks in services (a recruiter cannot touch another recruiter's job).
6. **What is the N+1 problem and how did you reduce it?** Lazy associations can trigger one query per row. I set `default_batch_fetch_size` and keep all mapping inside transactional service methods. `@EntityGraph` or fetch joins are the next step.
7. **Why `spring.jpa.open-in-view=false`?** It stops database sessions staying open during view rendering, so lazy loading must be handled deliberately in the service layer.
8. **How did you handle validation and errors?** Bean Validation annotations on request records, and a `@RestControllerAdvice` that maps each exception type to a proper HTTP status and JSON body.
9. **What would you improve next?** Refresh tokens, resume file upload to cloud storage, Flyway migrations, caching for popular searches, and CI with GitHub Actions.
10. **Why `@Async` for emails?** Sending mail is slow and can fail. Running it asynchronously keeps the API response fast and a mail error never breaks the status update.
