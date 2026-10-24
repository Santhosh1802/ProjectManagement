package com.Santhosh1802.ProjectManagement.repository;

import com.Santhosh1802.ProjectManagement.entity.ProjectMember;
import com.Santhosh1802.ProjectManagement.util.ProjectRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository
        extends JpaRepository<ProjectMember, UUID> {

    boolean existsByUserIdAndProjectId(
            UUID userId,
            UUID projectId
    );

    Page<ProjectMember> findByProjectId(
            UUID projectId,
            Pageable pageable
    );

    java.util.List<ProjectMember> findByProjectId(UUID projectId);

    Page<ProjectMember> findByUserId(
            UUID userId,
            Pageable pageable
    );

    Optional<ProjectMember> findByProjectIdAndUserId(
            UUID projectId,
            UUID userId
    );

    boolean existsByProjectIdAndUserEmail(
            UUID projectId,
            String email
    );

    boolean existsByProjectIdAndUserEmailAndProjectRole(
            UUID projectId,
            String email,
            ProjectRole projectRole
    );

    boolean existsByProjectIdAndUserId(
            UUID projectId,
            UUID userId
    );
}