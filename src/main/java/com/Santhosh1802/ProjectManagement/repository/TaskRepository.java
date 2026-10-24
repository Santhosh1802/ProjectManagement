package com.Santhosh1802.ProjectManagement.repository;

import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.Task;
import com.Santhosh1802.ProjectManagement.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    Page<Task> findByProject(Project project, Pageable pageable);

    Page<Task> findByProjectAndAssignedTo(Project project, User assignedTo, Pageable pageable);

    Page<Task> findByAssignedTo(User assignedTo, Pageable pageable);

    Page<Task> findByCreatedBy(User createdBy, Pageable pageable);

    List<Task> findByProjectId(UUID projectId);

    List<Task> findByProjectIdIn(java.util.Collection<UUID> projectIds);

    @org.springframework.data.jpa.repository.Query("SELECT t.taskStatus, COUNT(t) FROM Task t WHERE t.project.id IN :projectIds GROUP BY t.taskStatus")
    List<Object[]> countTasksByStatusForProjects(@org.springframework.data.repository.query.Param("projectIds") java.util.Collection<UUID> projectIds);

    @org.springframework.data.jpa.repository.Query("SELECT t.taskPriority, COUNT(t) FROM Task t WHERE t.project.id IN :projectIds GROUP BY t.taskPriority")
    List<Object[]> countTasksByPriorityForProjects(@org.springframework.data.repository.query.Param("projectIds") java.util.Collection<UUID> projectIds);

    @org.springframework.data.jpa.repository.Query("SELECT t.taskStatus, COUNT(t) FROM Task t WHERE t.project.id = :projectId GROUP BY t.taskStatus")
    List<Object[]> countTasksByStatusForProject(@org.springframework.data.repository.query.Param("projectId") UUID projectId);

    @org.springframework.data.jpa.repository.Query("SELECT t.taskPriority, COUNT(t) FROM Task t WHERE t.project.id = :projectId GROUP BY t.taskPriority")
    List<Object[]> countTasksByPriorityForProject(@org.springframework.data.repository.query.Param("projectId") UUID projectId);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM Task t WHERE t.project.id IN :projectIds AND t.dueDate IS NOT NULL AND t.dueDate < :now AND t.taskStatus NOT IN ('DONE', 'CANCELLED') ORDER BY t.dueDate ASC")
    List<Task> findOverdueTasksForProjects(@org.springframework.data.repository.query.Param("projectIds") java.util.Collection<UUID> projectIds, @org.springframework.data.repository.query.Param("now") java.time.Instant now, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM Task t WHERE t.project.id IN :projectIds AND t.dueDate IS NOT NULL AND t.dueDate >= :now AND t.taskStatus NOT IN ('DONE', 'CANCELLED') ORDER BY t.dueDate ASC")
    List<Task> findUpcomingTasksForProjects(@org.springframework.data.repository.query.Param("projectIds") java.util.Collection<UUID> projectIds, @org.springframework.data.repository.query.Param("now") java.time.Instant now, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.dueDate IS NOT NULL AND t.dueDate < :now AND t.taskStatus NOT IN ('DONE', 'CANCELLED') ORDER BY t.dueDate ASC")
    List<Task> findOverdueTasksForProject(@org.springframework.data.repository.query.Param("projectId") UUID projectId, @org.springframework.data.repository.query.Param("now") java.time.Instant now);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.dueDate IS NOT NULL AND t.dueDate >= :now AND t.taskStatus NOT IN ('DONE', 'CANCELLED') ORDER BY t.dueDate ASC")
    List<Task> findUpcomingTasksForProject(@org.springframework.data.repository.query.Param("projectId") UUID projectId, @org.springframework.data.repository.query.Param("now") java.time.Instant now, Pageable pageable);
}