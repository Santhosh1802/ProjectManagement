package com.Santhosh1802.ProjectManagement.repository;

import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.Task;
import com.Santhosh1802.ProjectManagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByProject(Project project);
    List<Task> findByAssignedTo(User assignedTo);
    List<Task> findByCreatedBy(User createdBy);
}
