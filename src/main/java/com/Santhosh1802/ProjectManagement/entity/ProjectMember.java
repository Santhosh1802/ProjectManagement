package com.Santhosh1802.ProjectManagement.entity;

import com.Santhosh1802.ProjectManagement.util.ProjectRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "project_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_project_member_user_project",
                        columnNames = {"user_id", "project_id"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    @Enumerated(EnumType.STRING)
    @Column(name = "project_role", nullable = false)
    private ProjectRole projectRole;
    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;
}
