package com.Santhosh1802.ProjectManagement.repository;

import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.util.UserRole;
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
public interface UserRepository extends JpaRepository<User, UUID> {
    public Optional<User> findByEmail(String email);

    @Query("""
    SELECT u
    FROM User u
    WHERE
        (
            :keyword IS NULL
            OR :keyword = ''
            OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        AND (
            :role IS NULL
            OR u.userRole = :role
        )
        AND (
            :isActive IS NULL
            OR u.isActive = :isActive
        )
        AND (
            :emailVerified IS NULL
            OR u.emailVerified = :emailVerified
        )
    """)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("role") UserRole role,
            @Param("isActive") Boolean isActive,
            @Param("emailVerified") Boolean emailVerified,
            Pageable pageable
    );
}
