package com.Santhosh1802.ProjectManagement.repository;

import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.ProjectMember;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.util.ProjectRole;
import com.Santhosh1802.ProjectManagement.util.ProjectStatus;
import com.Santhosh1802.ProjectManagement.util.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    private User owner;
    private User sharedUser;
    private User unrelatedUser;

    private Project activeProject;
    private Project planningProject;
    private Project completedProject;
    private Project sharedProject;

    private String testPrefix;

    @BeforeEach
    void setUp() {

        /*
         * We are using the existing PostgreSQL database.
         *
         * Do NOT call deleteAll() because existing projects/users
         * may already be referenced by other entities.
         *
         * Every test gets unique data instead.
         */
        testPrefix = "project-repo-test-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8);

        // ---------------------------------------------------------
        // Users
        // ---------------------------------------------------------

        owner = createUser(
                testPrefix + "-owner",
                "Owner",
                testPrefix + "-owner@test.com"
        );

        sharedUser = createUser(
                testPrefix + "-shared",
                "User",
                testPrefix + "-shared@test.com"
        );

        unrelatedUser = createUser(
                testPrefix + "-unrelated",
                "User",
                testPrefix + "-unrelated@test.com"
        );

        userRepository.saveAll(
                List.of(owner, sharedUser, unrelatedUser)
        );

        // ---------------------------------------------------------
        // Projects
        // ---------------------------------------------------------

        activeProject = createProject(
                testPrefix + "-active",
                "Active project description",
                ProjectStatus.ACTIVE,
                owner,
                Instant.now().plus(10, ChronoUnit.DAYS)
        );

        planningProject = createProject(
                testPrefix + "-planning",
                "Planning project description",
                ProjectStatus.PLANNING,
                owner,
                Instant.now().plus(20, ChronoUnit.DAYS)
        );

        completedProject = createProject(
                testPrefix + "-completed",
                "Completed project description",
                ProjectStatus.COMPLETED,
                owner,
                Instant.now().minus(10, ChronoUnit.DAYS)
        );

        sharedProject = createProject(
                testPrefix + "-shared",
                "Shared project description",
                ProjectStatus.ACTIVE,
                owner,
                Instant.now().plus(30, ChronoUnit.DAYS)
        );

        projectRepository.saveAll(
                List.of(
                        activeProject,
                        planningProject,
                        completedProject,
                        sharedProject
                )
        );

        // ---------------------------------------------------------
        // Project member
        // ---------------------------------------------------------

        ProjectMember projectMember = new ProjectMember();

        projectMember.setUser(sharedUser);
        projectMember.setProject(sharedProject);
        projectMember.setProjectRole(ProjectRole.MEMBER);

        projectMemberRepository.save(projectMember);
    }

    // =========================================================
    // findProjectByName
    // =========================================================

    @Test
    void findProjectByName_shouldReturnProject_whenNameExists() {

        Optional<Project> result =
                projectRepository.findProjectByName(
                        activeProject.getName()
                );

        assertThat(result).isPresent();

        assertThat(result.get().getName())
                .isEqualTo(activeProject.getName());

        assertThat(result.get().getId())
                .isEqualTo(activeProject.getId());
    }

    @Test
    void findProjectByName_shouldReturnEmpty_whenNameDoesNotExist() {

        Optional<Project> result =
                projectRepository.findProjectByName(
                        testPrefix + "-not-found"
                );

        assertThat(result).isEmpty();
    }

    // =========================================================
    // findProjectsByOwner
    // =========================================================

    @Test
    void findProjectsByOwner_shouldReturnProjectsOwnedByUser() {

        List<Project> result =
                projectRepository.findProjectsByOwner(owner);

        assertThat(result)
                .hasSize(4)
                .extracting(Project::getName)
                .containsExactlyInAnyOrder(
                        activeProject.getName(),
                        planningProject.getName(),
                        completedProject.getName(),
                        sharedProject.getName()
                );
    }

    @Test
    void findProjectsByOwner_shouldReturnEmpty_whenOwnerHasNoProjects() {

        List<Project> result =
                projectRepository.findProjectsByOwner(unrelatedUser);

        assertThat(result).isEmpty();
    }

    // =========================================================
    // findByOwnerId
    // =========================================================

    @Test
    void findByOwnerId_shouldReturnProjectsOwnedByUser() {

        List<Project> result =
                projectRepository.findByOwnerId(owner.getId());

        assertThat(result)
                .hasSize(4)
                .extracting(Project::getName)
                .containsExactlyInAnyOrder(
                        activeProject.getName(),
                        planningProject.getName(),
                        completedProject.getName(),
                        sharedProject.getName()
                );
    }

    @Test
    void findByOwnerId_shouldReturnEmpty_whenOwnerDoesNotExist() {

        UUID unknownUserId = UUID.randomUUID();

        List<Project> result =
                projectRepository.findByOwnerId(unknownUserId);

        assertThat(result).isEmpty();
    }

    // =========================================================
    // searchProjects
    // =========================================================

    @Test
    void searchProjects_shouldFindProjectByKeyword() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Project> result =
                projectRepository.searchProjects(
                        testPrefix + "-active",
                        null,
                        null,
                        "ALL",
                        pageable
                );

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(Project::getName)
                .containsExactly(activeProject.getName());
    }

    @Test
    void searchProjects_shouldBeCaseInsensitive() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Project> result =
                projectRepository.searchProjects(
                        (testPrefix + "-ACTIVE").toUpperCase(),
                        null,
                        null,
                        "ALL",
                        pageable
                );

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(Project::getName)
                .containsExactly(activeProject.getName());
    }

    @Test
    void searchProjects_shouldFilterByStatus() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Project> result =
                projectRepository.searchProjects(
                        testPrefix,
                        ProjectStatus.PLANNING,
                        null,
                        "ALL",
                        pageable
                );

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(Project::getName)
                .containsExactly(planningProject.getName());
    }

    @Test
    void searchProjects_shouldFilterByOwner() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Project> result =
                projectRepository.searchProjects(
                        testPrefix,
                        null,
                        owner.getId(),
                        "ALL",
                        pageable
                );

        assertThat(result.getContent())
                .hasSize(4);
    }

    @Test
    void searchProjects_shouldReturnEmpty_whenFiltersDoNotMatch() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Project> result =
                projectRepository.searchProjects(
                        testPrefix + "-active",
                        ProjectStatus.COMPLETED,
                        owner.getId(),
                        "ALL",
                        pageable
                );

        assertThat(result.getContent()).isEmpty();
    }

    // =========================================================
    // searchProjects - due date filters
    // =========================================================

    @Test
    void searchProjects_shouldFindAll_whenDueDateFilterIsAll() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Project> result =
                projectRepository.searchProjects(
                        testPrefix,
                        null,
                        owner.getId(),
                        "ALL",
                        pageable
                );

        assertThat(result.getContent())
                .hasSize(4);
    }

    @Test
    void searchProjects_shouldFindOverdueProjects() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Project> result =
                projectRepository.searchProjects(
                        testPrefix,
                        null,
                        owner.getId(),
                        "OVERDUE",
                        pageable
                );

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(Project::getName)
                .containsExactly(completedProject.getName());
    }

    @Test
    void searchProjects_shouldFindProjectsDueToday() {

        /*
         * PostgreSQL evaluates DUE_TODAY using CURRENT_DATE.
         *
         * Therefore, we need to construct the due date explicitly for
         * today's PostgreSQL/local calendar date rather than using
         * Instant.now().plus(...), which can cross a date boundary
         * depending on timezone conversion.
         */
        java.time.LocalDate today = java.time.LocalDate.now();

        java.time.LocalDateTime todayEnd =
                today.atTime(23, 59, 59);

        Instant dueToday =
                todayEnd.atZone(
                        java.time.ZoneId.systemDefault()
                ).toInstant();

        Project dueTodayProject = createProject(
                testPrefix + "-today",
                "Project due today",
                ProjectStatus.ACTIVE,
                owner,
                dueToday
        );

        projectRepository.save(dueTodayProject);

        Pageable pageable = PageRequest.of(0, 20);

        Page<Project> result =
                projectRepository.searchProjects(
                        testPrefix + "-today",
                        null,
                        owner.getId(),
                        "DUE_TODAY",
                        pageable
                );

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(Project::getName)
                .containsExactly(dueTodayProject.getName());
    }

    @Test
    void searchProjects_shouldSupportPagination() {

        Pageable pageable = PageRequest.of(0, 2);

        Page<Project> result =
                projectRepository.searchProjects(
                        testPrefix,
                        null,
                        owner.getId(),
                        "ALL",
                        pageable
                );

        assertThat(result.getSize())
                .isEqualTo(2);

        assertThat(result.getNumberOfElements())
                .isEqualTo(2);

        assertThat(result.getTotalElements())
                .isEqualTo(4);

        assertThat(result.getTotalPages())
                .isEqualTo(2);
    }

    // =========================================================
    // searchAllProjects
    // =========================================================

    @Test
    void searchAllProjects_shouldFindProjectsByKeyword() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Project> result =
                projectRepository.searchAllProjects(
                        testPrefix,
                        null,
                        null,
                        pageable
                );

        assertThat(result.getContent())
                .hasSize(4);
    }

    @Test
    void searchAllProjects_shouldFilterByStatus() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Project> result =
                projectRepository.searchAllProjects(
                        testPrefix,
                        ProjectStatus.ACTIVE,
                        null,
                        pageable
                );

        assertThat(result.getContent())
                .hasSize(2)
                .extracting(Project::getName)
                .containsExactlyInAnyOrder(
                        activeProject.getName(),
                        sharedProject.getName()
                );
    }

    @Test
    void searchAllProjects_shouldFilterByOwner() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Project> result =
                projectRepository.searchAllProjects(
                        testPrefix,
                        null,
                        owner.getId(),
                        pageable
                );

        assertThat(result.getContent())
                .hasSize(4);
    }

    // =========================================================
    // searchOwnedProjects
    // =========================================================

    @Test
    void searchOwnedProjects_shouldReturnProjectsOwnedByUser() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Project> result =
                projectRepository.searchOwnedProjects(
                        owner.getId(),
                        testPrefix,
                        null,
                        pageable
                );

        assertThat(result.getContent())
                .hasSize(4);
    }

    @Test
    void searchOwnedProjects_shouldFilterByStatus() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Project> result =
                projectRepository.searchOwnedProjects(
                        owner.getId(),
                        testPrefix,
                        ProjectStatus.ACTIVE,
                        pageable
                );

        assertThat(result.getContent())
                .hasSize(2)
                .extracting(Project::getName)
                .containsExactlyInAnyOrder(
                        activeProject.getName(),
                        sharedProject.getName()
                );
    }

    @Test
    void searchOwnedProjects_shouldReturnEmpty_forUserWhoOwnsNoProjects() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Project> result =
                projectRepository.searchOwnedProjects(
                        unrelatedUser.getId(),
                        testPrefix,
                        null,
                        pageable
                );

        assertThat(result.getContent()).isEmpty();
    }

    // =========================================================
    // searchSharedProjects
    // =========================================================

    @Test
    void searchSharedProjects_shouldReturnProjectsSharedWithUser() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Project> result =
                projectRepository.searchSharedProjects(
                        sharedUser.getId(),
                        testPrefix,
                        null,
                        pageable
                );

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(Project::getName)
                .containsExactly(sharedProject.getName());
    }

    @Test
    void searchSharedProjects_shouldFilterByStatus() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Project> result =
                projectRepository.searchSharedProjects(
                        sharedUser.getId(),
                        testPrefix,
                        ProjectStatus.ACTIVE,
                        pageable
                );

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(Project::getName)
                .containsExactly(sharedProject.getName());
    }

    @Test
    void searchSharedProjects_shouldReturnEmpty_forUnrelatedUser() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Project> result =
                projectRepository.searchSharedProjects(
                        unrelatedUser.getId(),
                        testPrefix,
                        null,
                        pageable
                );

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void searchSharedProjects_shouldNotReturnOwnedProjects() {

        /*
         * sharedUser is a member of sharedProject.
         *
         * The query contains:
         *
         * p.owner.id <> :userId
         *
         * so a user's own projects must not appear in shared projects.
         */
        Pageable pageable = PageRequest.of(0, 10);

        Page<Project> result =
                projectRepository.searchSharedProjects(
                        owner.getId(),
                        testPrefix,
                        null,
                        pageable
                );

        assertThat(result.getContent()).isEmpty();
    }

    // =========================================================
    // Helpers
    // =========================================================

    private User createUser(
            String firstName,
            String lastName,
            String email
    ) {

        User user = new User();

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword("test-password");
        user.setUserRole(UserRole.USER);
        user.setIsActive(true);
        user.setEmailVerified(true);

        return user;
    }

    private Project createProject(
            String name,
            String description,
            ProjectStatus status,
            User owner,
            Instant dueDate
    ) {

        Project project = new Project();

        project.setName(name);
        project.setDescription(description);
        project.setProjectStatus(status);
        project.setStartDate(
                Instant.now().minus(5, ChronoUnit.DAYS)
        );
        project.setDueDate(dueDate);
        project.setOwner(owner);

        return project;
    }
}