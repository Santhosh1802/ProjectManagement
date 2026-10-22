package com.Santhosh1802.ProjectManagement.audit.repository;

import com.Santhosh1802.ProjectManagement.audit.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

    List<ActivityLog> findByTaskIdOrderByCreatedAtDesc(UUID taskId);

    List<ActivityLog> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
