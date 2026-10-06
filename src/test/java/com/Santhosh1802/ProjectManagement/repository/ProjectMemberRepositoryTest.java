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
class ProjectMemberRepositoryTest {

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User member;
    private User manager;
    private User unrelatedUser;

    private Project project;
    private Project secondProject;

    private ProjectMember memberRecord;
    private ProjectMember managerRecord;

    private String testPrefix;

    @BeforeEach
    void setUp() {

        /*
         * We are using the existing PostgreSQL database.
         *
         * Do NOT use deleteAll().
         * Existing users/projects may already be referenced by
         * project_members, tasks, etc.
         *
         * Every test receives unique test data.
         */
        testPrefix = "member-repo-test-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8);

        // =========================================================
        // Users
        // =========================================================

        owner = createUser(
                testPrefix + "-owner",
                "Owner",
                testPrefix + "-owner@test.com"
        );

        member = createUser(
                testPrefix + "-member",
                "Member",
                testPrefix + "-member@test.com"
        );

        manager = createUser(
                testPrefix + "-manager",
                "Manager",
                testPrefix + "-manager@test.com"
        );

        unrelatedUser = createUser(
                testPrefix + "-unrelated",
                "User",
                testPrefix + "-unrelated@test.com"
        );

        userRepository.saveAll(
                List.of(
                        owner,
                        member,
                        manager,
                        unrelatedUser
                )
        );

        // =========================================================
        // Projects
        // =========================================================

        project = createProject(
                testPrefix + "-project",
                owner
        );

        secondProject = createProject(
                testPrefix + "-second-project",
                owner
        );

        projectRepository.saveAll(
                List.of(
                        project,
                        secondProject
                )
        );

        // =========================================================
        // Project Members
        // =========================================================

        memberRecord = createProjectMember(
                member,
                project,
                ProjectRole.MEMBER
        );

        managerRecord = createProjectMember(
                manager,
                project,
                ProjectRole.MANAGER
        );

        projectMemberRepository.saveAll(
                List.of(
                        memberRecord,
                        managerRecord
                )
        );
    }

    // =========================================================
    // existsByUserIdAndProjectId
    // =========================================================

    @Test
    void existsByUserIdAndProjectId_shouldReturnTrue_whenMemberExists() {

        boolean result =
                projectMemberRepository.existsByUserIdAndProjectId(
                        member.getId(),
                        project.getId()
                );

        assertThat(result).isTrue();
    }

    @Test
    void existsByUserIdAndProjectId_shouldReturnFalse_whenMemberDoesNotExist() {

        boolean result =
                projectMemberRepository.existsByUserIdAndProjectId(
                        unrelatedUser.getId(),
                        project.getId()
                );

        assertThat(result).isFalse();
    }

    @Test
    void existsByUserIdAndProjectId_shouldReturnFalse_whenProjectDoesNotExist() {

        boolean result =
                projectMemberRepository.existsByUserIdAndProjectId(
                        member.getId(),
                        UUID.randomUUID()
                );

        assertThat(result).isFalse();
    }

    // =========================================================
    // findByProjectId
    // =========================================================

    @Test
    void findByProjectId_shouldReturnProjectMembers() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<ProjectMember> result =
                projectMemberRepository.findByProjectId(
                        project.getId(),
                        pageable
                );

        assertThat(result.getContent())
                .hasSize(2)
                .extracting(ProjectMember::getUser)
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder(
                        member.getEmail(),
                        manager.getEmail()
                );
    }

    @Test
    void findByProjectId_shouldReturnEmpty_whenProjectHasNoMembers() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<ProjectMember> result =
                projectMemberRepository.findByProjectId(
                        secondProject.getId(),
                        pageable
                );

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findByProjectId_shouldSupportPagination() {

        Pageable pageable = PageRequest.of(0, 1);

        Page<ProjectMember> result =
                projectMemberRepository.findByProjectId(
                        project.getId(),
                        pageable
                );

        assertThat(result.getSize())
                .isEqualTo(1);

        assertThat(result.getNumberOfElements())
                .isEqualTo(1);

        assertThat(result.getTotalElements())
                .isEqualTo(2);

        assertThat(result.getTotalPages())
                .isEqualTo(2);
    }

    // =========================================================
    // findByUserId
    // =========================================================

    @Test
    void findByUserId_shouldReturnProjectsForUser() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<ProjectMember> result =
                projectMemberRepository.findByUserId(
                        member.getId(),
                        pageable
                );

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(ProjectMember::getProject)
                .extracting(Project::getName)
                .containsExactly(project.getName());
    }

    @Test
    void findByUserId_shouldReturnEmpty_whenUserIsNotMember() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<ProjectMember> result =
                projectMemberRepository.findByUserId(
                        unrelatedUser.getId(),
                        pageable
                );

        assertThat(result.getContent()).isEmpty();
    }

    // =========================================================
    // findByProjectIdAndUserId
    // =========================================================

    @Test
    void findByProjectIdAndUserId_shouldReturnMember_whenExists() {

        Optional<ProjectMember> result =
                projectMemberRepository.findByProjectIdAndUserId(
                        project.getId(),
                        member.getId()
                );

        assertThat(result).isPresent();

        assertThat(result.get().getId())
                .isEqualTo(memberRecord.getId());

        assertThat(result.get().getProjectRole())
                .isEqualTo(ProjectRole.MEMBER);
    }

    @Test
    void findByProjectIdAndUserId_shouldReturnEmpty_whenMemberDoesNotExist() {

        Optional<ProjectMember> result =
                projectMemberRepository.findByProjectIdAndUserId(
                        project.getId(),
                        unrelatedUser.getId()
                );

        assertThat(result).isEmpty();
    }

    @Test
    void findByProjectIdAndUserId_shouldReturnEmpty_whenProjectDoesNotExist() {

        Optional<ProjectMember> result =
                projectMemberRepository.findByProjectIdAndUserId(
                        UUID.randomUUID(),
                        member.getId()
                );

        assertThat(result).isEmpty();
    }

    // =========================================================
    // existsByProjectIdAndUserEmail
    // =========================================================

    @Test
    void existsByProjectIdAndUserEmail_shouldReturnTrue_whenMemberExists() {

        boolean result =
                projectMemberRepository.existsByProjectIdAndUserEmail(
                        project.getId(),
                        member.getEmail()
                );

        assertThat(result).isTrue();
    }

    @Test
    void existsByProjectIdAndUserEmail_shouldReturnFalse_whenUserIsNotMember() {

        boolean result =
                projectMemberRepository.existsByProjectIdAndUserEmail(
                        project.getId(),
                        unrelatedUser.getEmail()
                );

        assertThat(result).isFalse();
    }

    @Test
    void existsByProjectIdAndUserEmail_shouldReturnFalse_whenEmailDoesNotExist() {

        boolean result =
                projectMemberRepository.existsByProjectIdAndUserEmail(
                        project.getId(),
                        testPrefix + "-notfound@test.com"
                );

        assertThat(result).isFalse();
    }

    // =========================================================
    // existsByProjectIdAndUserEmailAndProjectRole
    // =========================================================

    @Test
    void existsByProjectIdAndUserEmailAndProjectRole_shouldReturnTrue_forMemberRole() {

        boolean result =
                projectMemberRepository
                        .existsByProjectIdAndUserEmailAndProjectRole(
                                project.getId(),
                                member.getEmail(),
                                ProjectRole.MEMBER
                        );

        assertThat(result).isTrue();
    }

    @Test
    void existsByProjectIdAndUserEmailAndProjectRole_shouldReturnTrue_forManagerRole() {

        boolean result =
                projectMemberRepository
                        .existsByProjectIdAndUserEmailAndProjectRole(
                                project.getId(),
                                manager.getEmail(),
                                ProjectRole.MANAGER
                        );

        assertThat(result).isTrue();
    }

    @Test
    void existsByProjectIdAndUserEmailAndProjectRole_shouldReturnFalse_whenRoleDoesNotMatch() {

        boolean result =
                projectMemberRepository
                        .existsByProjectIdAndUserEmailAndProjectRole(
                                project.getId(),
                                member.getEmail(),
                                ProjectRole.MANAGER
                        );

        assertThat(result).isFalse();
    }

    @Test
    void existsByProjectIdAndUserEmailAndProjectRole_shouldReturnFalse_whenUserIsNotMember() {

        boolean result =
                projectMemberRepository
                        .existsByProjectIdAndUserEmailAndProjectRole(
                                project.getId(),
                                unrelatedUser.getEmail(),
                                ProjectRole.MEMBER
                        );

        assertThat(result).isFalse();
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
            User owner
    ) {

        Project project = new Project();

        project.setName(name);
        project.setDescription(
                testPrefix + " project description"
        );
        project.setProjectStatus(ProjectStatus.ACTIVE);
        project.setStartDate(
                Instant.now().minus(5, ChronoUnit.DAYS)
        );
        project.setDueDate(
                Instant.now().plus(30, ChronoUnit.DAYS)
        );
        project.setOwner(owner);

        return project;
    }

    private ProjectMember createProjectMember(
            User user,
            Project project,
            ProjectRole projectRole
    ) {

        ProjectMember projectMember = new ProjectMember();

        projectMember.setUser(user);
        projectMember.setProject(project);
        projectMember.setProjectRole(projectRole);

        return projectMember;
    }
}