package com.Santhosh1802.ProjectManagement.security;


import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.ProjectMember;
import com.Santhosh1802.ProjectManagement.entity.Task;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.repository.ProjectMemberRepository;
import com.Santhosh1802.ProjectManagement.repository.ProjectRepository;
import com.Santhosh1802.ProjectManagement.repository.TaskRepository;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private TaskRepository taskRepository;

    private User owner;
    private User manager;
    private User member;
    private User assignedUser;
    private User normalUser;
    private Project project;
    private Task task;

    @BeforeEach
    void setUp() {

        owner = createUser("owner@example.com");
        manager = createUser("manager@example.com");
        member = createUser("member@example.com");
        assignedUser = createUser("assigned@example.com");
        normalUser = createUser("user@example.com");

        userRepository.save(owner);
        userRepository.save(manager);
        userRepository.save(member);
        userRepository.save(assignedUser);
        userRepository.save(normalUser);

        project = createProject(owner);

        ProjectMember managerMember = new ProjectMember();
        managerMember.setUser(manager);
        managerMember.setProject(project);
        managerMember.setProjectRole(ProjectRole.MANAGER);

        projectMemberRepository.save(managerMember);

        ProjectMember normalMember = new ProjectMember();
        normalMember.setUser(member);
        normalMember.setProject(project);
        normalMember.setProjectRole(ProjectRole.MEMBER);

        projectMemberRepository.save(normalMember);

        task = new Task();

        task.setTitle("Security Task");
        task.setDescription("Task used for security integration tests");
        task.setTaskStatus(TaskStatus.TODO);
        task.setTaskPriority(TaskPriority.MEDIUM);
        task.setDueDate(Instant.parse("2026-12-31T10:00:00Z"));
        task.setProject(project);
        task.setAssignedTo(assignedUser);
        task.setCreatedBy(owner);

        task = taskRepository.save(task);
    }

    private User createUser(String email) {

        User user = new User();

        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(email);
        user.setPassword("$2a$12$abcdefghijklmnopqrstuv");
        user.setUserRole(UserRole.USER);
        user.setIsActive(true);
        user.setEmailVerified(true);

        return user;
    }
    private Project createProject(User owner) {
        Project project = new Project();

        project.setName("Security Test Project");
        project.setDescription("Project for security testing");
        project.setProjectStatus(ProjectStatus.ACTIVE);
        project.setStartDate(Instant.now());
        project.setDueDate(Instant.now().plus(30, ChronoUnit.DAYS));
        project.setOwner(owner);

        return projectRepository.save(project);
    }

    // ---------------------------------------------------------
    // UNAUTHENTICATED
    // ---------------------------------------------------------

    @Test
    void unauthenticatedUser_shouldReceive401() throws Exception {

        mockMvc.perform(
                        get("/api/tasks/id")
                                .param("id", task.getId().toString())
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedUser_shouldReceive401WhenCreatingTask()
            throws Exception {

        mockMvc.perform(
                        post("/api/tasks/create")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                              "title": "New Task",
                              "description": "New task description",
                              "taskStatus": "TODO",
                              "taskPriority": "MEDIUM",
                              "dueDate": "2026-12-31T10:00:00Z",
                              "projectId": "%s",
                              "assignedToId": "%s",
                              "createdById": "%s"
                            }
                            """.formatted(
                                        project.getId(),
                                        assignedUser.getId(),
                                        owner.getId()
                                ))
                )
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------
    // CSRF
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "owner@example.com")
    void authenticatedUser_withoutCsrf_shouldReceive403()
            throws Exception {

        mockMvc.perform(
                        post("/api/tasks/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "title": "New Task",
                                          "description": "New task description",
                                          "taskStatus": "TODO",
                                          "taskPriority": "MEDIUM",
                                          "dueDate": "2026-12-31T10:00:00Z",
                                          "projectId": "%s",
                                          "assignedToId": "%s",
                                          "createdById": "%s"
                                        }
                                        """.formatted(
                                        project.getId(),
                                        assignedUser.getId(),
                                        owner.getId()
                                ))
                )
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------
    // GET TASK
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "owner@example.com")
    void projectOwner_shouldViewTask() throws Exception {

        mockMvc.perform(
                        get("/api/tasks/id")
                                .param("id", task.getId().toString())
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "manager@example.com")
    void projectManager_shouldViewTask() throws Exception {

        mockMvc.perform(
                        get("/api/tasks/id")
                                .param("id", task.getId().toString())
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "member@example.com")
    void projectMember_shouldViewTask() throws Exception {

        mockMvc.perform(
                        get("/api/tasks/id")
                                .param("id", task.getId().toString())
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "assigned@example.com")
    void assignedUser_shouldViewTask() throws Exception {

        mockMvc.perform(
                        get("/api/tasks/id")
                                .param("id", task.getId().toString())
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void unrelatedUser_shouldNotViewTask() throws Exception {

        mockMvc.perform(
                        get("/api/tasks/id")
                                .param("id", task.getId().toString())
                )
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------
    // GET ALL TASKS
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "user@example.com")
    void normalUser_shouldNotViewAllTasks() throws Exception {

        mockMvc.perform(
                        get("/api/tasks/all")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = "ADMIN"
    )
    void admin_shouldViewAllTasks() throws Exception {

        mockMvc.perform(
                        get("/api/tasks/all")
                )
                .andExpect(status().isOk());
    }

    // ---------------------------------------------------------
    // PROJECT TASKS
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "owner@example.com")
    void owner_shouldViewProjectTasks() throws Exception {

        mockMvc.perform(
                        get("/api/tasks/by-project")
                                .param(
                                        "projectId",
                                        project.getId().toString()
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "member@example.com")
    void member_shouldViewProjectTasks() throws Exception {

        mockMvc.perform(
                        get("/api/tasks/by-project")
                                .param(
                                        "projectId",
                                        project.getId().toString()
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void unrelatedUser_shouldNotViewProjectTasks() throws Exception {

        mockMvc.perform(
                        get("/api/tasks/by-project")
                                .param(
                                        "projectId",
                                        project.getId().toString()
                                )
                )
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------
    // ASSIGNED TASKS
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "assigned@example.com")
    void assignedUser_shouldViewOwnAssignedTasks()
            throws Exception {

        mockMvc.perform(
                        get("/api/tasks/assigned-to")
                                .param(
                                        "assignedToId",
                                        assignedUser.getId().toString()
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void user_shouldNotViewAnotherUsersAssignedTasks()
            throws Exception {

        mockMvc.perform(
                        get("/api/tasks/assigned-to")
                                .param(
                                        "assignedToId",
                                        assignedUser.getId().toString()
                                )
                )
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------
    // CREATED TASKS
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "owner@example.com")
    void creator_shouldViewOwnCreatedTasks()
            throws Exception {

        mockMvc.perform(
                        get("/api/tasks/created-by")
                                .param(
                                        "createdByUserId",
                                        owner.getId().toString()
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void user_shouldNotViewAnotherUsersCreatedTasks()
            throws Exception {

        mockMvc.perform(
                        get("/api/tasks/created-by")
                                .param(
                                        "createdByUserId",
                                        owner.getId().toString()
                                )
                )
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "owner@example.com")
    void owner_shouldCreateTask() throws Exception {

        mockMvc.perform(
                        post("/api/tasks/create")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "title": "New Task",
                                          "description": "New task description",
                                          "taskStatus": "TODO",
                                          "taskPriority": "MEDIUM",
                                          "dueDate": "2026-12-31T10:00:00Z",
                                          "projectId": "%s",
                                          "assignedToId": "%s",
                                          "createdById": "%s"
                                        }
                                        """.formatted(
                                        project.getId(),
                                        assignedUser.getId(),
                                        owner.getId()
                                ))
                )
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "member@example.com")
    void normalProjectMember_shouldNotCreateTask()
            throws Exception {

        mockMvc.perform(
                        post("/api/tasks/create")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "title": "New Task",
                                          "description": "New task description",
                                          "taskStatus": "TODO",
                                          "taskPriority": "MEDIUM",
                                          "dueDate": "2026-12-31T10:00:00Z",
                                          "projectId": "%s",
                                          "assignedToId": "%s",
                                          "createdById": "%s"
                                        }
                                        """.formatted(
                                        project.getId(),
                                        assignedUser.getId(),
                                        member.getId()
                                ))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager@example.com")
    void projectManager_shouldCreateTask()
            throws Exception {

        mockMvc.perform(
                        post("/api/tasks/create")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "title": "Manager Task",
                                          "description": "Task created by manager",
                                          "taskStatus": "TODO",
                                          "taskPriority": "MEDIUM",
                                          "dueDate": "2026-12-31T10:00:00Z",
                                          "projectId": "%s",
                                          "assignedToId": "%s",
                                          "createdById": "%s"
                                        }
                                        """.formatted(
                                        project.getId(),
                                        assignedUser.getId(),
                                        manager.getId()
                                ))
                )
                .andExpect(status().isCreated());
    }

    // ---------------------------------------------------------
    // UPDATE DETAILS
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "owner@example.com")
    void owner_shouldUpdateTaskDetails()
            throws Exception {

        mockMvc.perform(
                        put("/api/tasks/update-details")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "id": "%s",
                                          "title": "Updated Task",
                                          "description": "Updated task description",
                                          "taskStatus": "IN_PROGRESS",
                                          "taskPriority": "HIGH",
                                          "dueDate": "2026-12-31T10:00:00Z",
                                          "projectId": "%s",
                                          "assignedToId": "%s",
                                          "createdById": "%s"
                                        }
                                        """.formatted(
                                        task.getId(),
                                        project.getId(),
                                        assignedUser.getId(),
                                        owner.getId()
                                ))
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "member@example.com")
    void member_shouldNotUpdateTaskDetails()
            throws Exception {

        mockMvc.perform(
                        put("/api/tasks/update-details")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "id": "%s",
                                          "title": "Updated Task",
                                          "description": "Updated task description",
                                          "taskStatus": "DONE",
                                          "taskPriority": "HIGH",
                                          "dueDate": "2026-12-31T10:00:00Z",
                                          "projectId": "%s",
                                          "assignedToId": "%s",
                                          "createdById": "%s"
                                        }
                                        """.formatted(
                                        task.getId(),
                                        project.getId(),
                                        assignedUser.getId(),
                                        member.getId()
                                ))
                )
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------
    // UPDATE STATUS
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "assigned@example.com")
    void assignedUser_shouldUpdateTaskStatus()
            throws Exception {

        mockMvc.perform(
                        put("/api/tasks/update-status")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "id": "%s",
                                          "status": "DONE"
                                        }
                                        """.formatted(task.getId()))
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "member@example.com")
    void member_shouldNotUpdateTaskStatus()
            throws Exception {

        mockMvc.perform(
                        put("/api/tasks/update-status")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "id": "%s",
                                          "status": "DONE"
                                        }
                                        """.formatted(task.getId()))
                )
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------

    @Test
    @WithMockUser(username = "owner@example.com")
    void owner_shouldDeleteTask()
            throws Exception {

        mockMvc.perform(
                        delete("/api/tasks/delete")
                                .with(csrf())
                                .param(
                                        "id",
                                        task.getId().toString()
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "member@example.com")
    void member_shouldNotDeleteTask()
            throws Exception {

        mockMvc.perform(
                        delete("/api/tasks/delete")
                                .with(csrf())
                                .param(
                                        "id",
                                        task.getId().toString()
                                )
                )
                .andExpect(status().isForbidden());
    }
}