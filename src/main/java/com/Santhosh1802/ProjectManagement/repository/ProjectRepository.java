package com.Santhosh1802.ProjectManagement.repository;

import com.Santhosh1802.ProjectManagement.entity.Project;
import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.util.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Optional<Project> findProjectByName(String name);
    List<Project> findProjectsByOwner(User user);
    List<Project> findByOwnerId(UUID ownerId);

    @Query("""
    SELECT p
    FROM Project p
    WHERE
        (
            :keyword IS NULL
            OR :keyword = ''
            OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.owner.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.owner.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.owner.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        AND (
            :status IS NULL
            OR p.projectStatus = :status
        )
        AND (
            :ownerId IS NULL
            OR p.owner.id = :ownerId
        )
        AND (
            :dueDateFilter = 'ALL'
            OR (
                :dueDateFilter = 'OVERDUE'
                AND p.dueDate < CURRENT_TIMESTAMP
            )
            OR (
                :dueDateFilter = 'DUE_TODAY'
                AND CAST(p.dueDate AS date) = CURRENT_DATE
            )
            OR (
                :dueDateFilter = 'DUE_THIS_MONTH'
                AND EXTRACT(MONTH FROM p.dueDate) = EXTRACT(MONTH FROM CURRENT_DATE)
                AND EXTRACT(YEAR FROM p.dueDate) = EXTRACT(YEAR FROM CURRENT_DATE)
            )
        )
    """)
    Page<Project> searchProjects(
            @Param("keyword") String keyword,
            @Param("status") ProjectStatus status,
            @Param("ownerId") UUID ownerId,
            @Param("dueDateFilter") String dueDateFilter,
            Pageable pageable
    );

    @Query("""
    SELECT p
    FROM Project p
    WHERE
        (
            :keyword IS NULL
            OR :keyword = ''
            OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.owner.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.owner.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.owner.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        AND (
            :status IS NULL
            OR p.projectStatus = :status
        )
        AND (
            :ownerId IS NULL
            OR p.owner.id = :ownerId
        )
    """)
    Page<Project> searchAllProjects(
            @Param("keyword") String keyword,
            @Param("status") ProjectStatus status,
            @Param("ownerId") UUID ownerId,
            Pageable pageable
    );

    @Query("""
    SELECT p
    FROM Project p
    WHERE
        p.owner.id = :userId
        AND (
            :keyword IS NULL
            OR :keyword = ''
            OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        AND (
            :status IS NULL
            OR p.projectStatus = :status
        )
    """)
    Page<Project> searchOwnedProjects(
            @Param("userId") UUID userId,
            @Param("keyword") String keyword,
            @Param("status") ProjectStatus status,
            Pageable pageable
    );
    @Query("""
    SELECT DISTINCT p
    FROM Project p
    JOIN ProjectMember pm
        ON pm.project = p
    WHERE
        pm.user.id = :userId
        AND p.owner.id <> :userId
        AND (
            :keyword IS NULL
            OR :keyword = ''
            OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        AND (
            :status IS NULL
            OR p.projectStatus = :status
        )
    """)
    Page<Project> searchSharedProjects(
            @Param("userId") UUID userId,
            @Param("keyword") String keyword,
            @Param("status") ProjectStatus status,
            Pageable pageable
    );
}
