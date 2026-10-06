package com.Santhosh1802.ProjectManagement.repository;

import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.Task;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.util.ProjectStatus;
import com.Santhosh1802.ProjectManagement.util.TaskPriority;
import com.Santhosh1802.ProjectManagement.util.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;


import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User assignedUser;
    private User creator;

    private Project project;

    private Task projectTask;
    private Task assignedTask;
    private Task createdTask;

    @BeforeEach
    void setUp() {

        owner = createUser("owner@example.com");
        assignedUser = createUser("assigned@example.com");
        creator = createUser("creator@example.com");

        owner = userRepository.save(owner);
        assignedUser = userRepository.save(assignedUser);
        creator = userRepository.save(creator);

        project = new Project();
        project.setName("Repository Test Project");
        project.setDescription("Project for repository testing");
        project.setProjectStatus(ProjectStatus.ACTIVE);
        project.setStartDate(Instant.parse("2026-10-01T00:00:00Z"));
        project.setDueDate(Instant.parse("2026-12-31T00:00:00Z"));
        project.setOwner(owner);

        project = projectRepository.save(project);

        projectTask = createTask(
                "Project Task",
                project,
                assignedUser,
                creator
        );

        assignedTask = createTask(
                "Assigned Task",
                project,
                assignedUser,
                owner
        );

        createdTask = createTask(
                "Created Task",
                project,
                owner,
                creator
        );

        taskRepository.save(projectTask);
        taskRepository.save(assignedTask);
        taskRepository.save(createdTask);
    }

    private User createUser(String email) {

        User user = new User();

        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(email);
        user.setPassword("password");
        user.setUserRole(
                com.Santhosh1802.ProjectManagement.util.UserRole.USER
        );
        user.setIsActive(true);
        user.setEmailVerified(true);

        return user;
    }

    private Task createTask(
            String title,
            Project project,
            User assignedTo,
            User createdBy
    ) {

        Task task = new Task();

        task.setTitle(title);
        task.setDescription("Repository test task");
        task.setTaskStatus(TaskStatus.TODO);
        task.setTaskPriority(TaskPriority.MEDIUM);
        task.setDueDate(
                Instant.parse("2026-12-31T10:00:00Z")
        );
        task.setProject(project);
        task.setAssignedTo(assignedTo);
        task.setCreatedBy(createdBy);

        return task;
    }

    @Test
    void findByProject_shouldReturnTasksForProject() {

        PageRequest pageable = PageRequest.of(0, 10);

        Page<Task> result =
                taskRepository.findByProject(
                        project,
                        pageable
                );

        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(3, result.getContent().size());

        assertTrue(
                result.getContent()
                        .stream()
                        .allMatch(task ->
                                task.getProject()
                                        .getId()
                                        .equals(project.getId())
                        )
        );
    }

    @Test
    void findByProject_shouldSupportPagination() {

        PageRequest pageable = PageRequest.of(0, 2);

        Page<Task> result =
                taskRepository.findByProject(
                        project,
                        pageable
                );

        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalPages());
        assertTrue(result.isFirst());
    }

    @Test
    void findByAssignedTo_shouldReturnAssignedTasks() {

        PageRequest pageable = PageRequest.of(0, 10);

        Page<Task> result =
                taskRepository.findByAssignedTo(
                        assignedUser,
                        pageable
                );

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        assertTrue(
                result.getContent()
                        .stream()
                        .allMatch(task ->
                                task.getAssignedTo()
                                        .getId()
                                        .equals(assignedUser.getId())
                        )
        );
    }

    @Test
    void findByAssignedTo_shouldReturnEmptyWhenUserHasNoTasks() {

        User userWithoutTasks =
                createUser("no-tasks@example.com");

        userWithoutTasks =
                userRepository.save(userWithoutTasks);

        PageRequest pageable = PageRequest.of(0, 10);

        Page<Task> result =
                taskRepository.findByAssignedTo(
                        userWithoutTasks,
                        pageable
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void findByCreatedBy_shouldReturnCreatedTasks() {

        PageRequest pageable = PageRequest.of(0, 10);

        Page<Task> result =
                taskRepository.findByCreatedBy(
                        creator,
                        pageable
                );

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        assertTrue(
                result.getContent()
                        .stream()
                        .allMatch(task ->
                                task.getCreatedBy()
                                        .getId()
                                        .equals(creator.getId())
                        )
        );
    }

    @Test
    void findByCreatedBy_shouldReturnEmptyWhenUserCreatedNoTasks() {

        User userWithoutTasks =
                createUser("no-created-tasks@example.com");

        userWithoutTasks =
                userRepository.save(userWithoutTasks);

        PageRequest pageable = PageRequest.of(0, 10);

        Page<Task> result =
                taskRepository.findByCreatedBy(
                        userWithoutTasks,
                        pageable
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }
}
