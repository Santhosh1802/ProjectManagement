package com.Santhosh1802.ProjectManagement.event;

import com.Santhosh1802.ProjectManagement.dto.request.task.CreateTaskRequest;
import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.repository.ProjectMemberRepository;
import com.Santhosh1802.ProjectManagement.repository.ProjectRepository;
import com.Santhosh1802.ProjectManagement.repository.TaskRepository;
import com.Santhosh1802.ProjectManagement.repository.UserRepository;
import com.Santhosh1802.ProjectManagement.service.TaskService;
import com.Santhosh1802.ProjectManagement.tasks.event.TaskCreatedEvent;
import com.Santhosh1802.ProjectManagement.util.ProjectStatus;
import com.Santhosh1802.ProjectManagement.util.TaskPriority;
import com.Santhosh1802.ProjectManagement.util.TaskStatus;
import com.Santhosh1802.ProjectManagement.util.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TaskEventTransactionIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private TestEventListener testEventListener;

    private User owner;
    private Project project;
    private Authentication auth;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TestEventListener testEventListener() {
            return new TestEventListener();
        }
    }

    public static class TestEventListener {
        private final List<TaskCreatedEvent> defaultEvents = new ArrayList<>();
        private final List<TaskCreatedEvent> commitPhaseEvents = new ArrayList<>();

        @EventListener
        public void onDefaultEvent(TaskCreatedEvent event) {
            defaultEvents.add(event);
        }

        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void onAfterCommit(TaskCreatedEvent event) {
            commitPhaseEvents.add(event);
        }

        public void reset() {
            defaultEvents.clear();
            commitPhaseEvents.clear();
        }

        public List<TaskCreatedEvent> getDefaultEvents() {
            return defaultEvents;
        }

        public List<TaskCreatedEvent> getCommitPhaseEvents() {
            return commitPhaseEvents;
        }
    }

    @BeforeEach
    void setUp() {
        testEventListener.reset();

        owner = new User();
        owner.setFirstName("Event");
        owner.setLastName("Tester");
        owner.setEmail("event.tester." + System.nanoTime() + "@example.com");
        owner.setPassword("encodedSecret123");
        owner.setUserRole(UserRole.ADMIN);
        owner.setIsActive(true);
        owner.setEmailVerified(true);
        owner = userRepository.save(owner);

        project = new Project();
        project.setName("Event Project " + System.nanoTime());
        project.setDescription("Testing transaction rollback event semantics");
        project.setProjectStatus(ProjectStatus.ACTIVE);
        project.setStartDate(Instant.now().minusSeconds(3600));
        project.setDueDate(Instant.now().plusSeconds(86400));
        project.setOwner(owner);
        project = projectRepository.save(project);

        auth = new UsernamePasswordAuthenticationToken(
                owner.getEmail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    @Test
    @DisplayName("Should receive after-commit event when transaction successfully commits")
    void testEventsOnSuccessfulCommit() {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        CreateTaskRequest request = new CreateTaskRequest(
                "Commit Task",
                "Testing commit",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                Instant.now().plusSeconds(3600),
                project.getId(),
                owner.getId()
        );

        taskService.createTask(request, auth);
        transactionManager.commit(status);

        assertEquals(1, testEventListener.getDefaultEvents().size(), "In-memory event published synchronously");
        assertEquals(1, testEventListener.getCommitPhaseEvents().size(), "After-commit listener receives event on successful commit");
    }

    @Test
    @DisplayName("Should NOT receive after-commit event when transaction rolls back after task creation")
    void testNoAfterCommitEventOnRollback() {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        CreateTaskRequest request = new CreateTaskRequest(
                "Rollback Task",
                "Testing rollback",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                Instant.now().plusSeconds(3600),
                project.getId(),
                owner.getId()
        );

        taskService.createTask(request, auth);

        // Synchronous @EventListener has executed in-memory as part of the invocation
        assertEquals(1, testEventListener.getDefaultEvents().size(), "Standard @EventListener executes in-memory immediately upon publication");

        // Simulate subsequent failure in the same outer transaction causing complete rollback
        transactionManager.rollback(status);

        // 1. Transactional event listener MUST NOT receive the after-commit event because the transaction aborted
        assertEquals(0, testEventListener.getCommitPhaseEvents().size(), "TransactionalEventListener(AFTER_COMMIT) must not fire on rollback");

        // 2. Database transaction rollback verification: Task must not exist in database
        List<com.Santhosh1802.ProjectManagement.entity.Task> rolledBackTasks = taskRepository
                .findByProject(project, org.springframework.data.domain.PageRequest.of(0, 10))
                .getContent();
        boolean taskPersisted = rolledBackTasks.stream().anyMatch(t -> "Rollback Task".equals(t.getTitle()));
        assertFalse(taskPersisted, "Rolled back task must NOT be persisted in the database");
    }
}
