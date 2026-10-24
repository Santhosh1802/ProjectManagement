package com.Santhosh1802.ProjectManagement.notification.repository;

import com.Santhosh1802.ProjectManagement.entity.User;
import com.Santhosh1802.ProjectManagement.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);

    Page<Notification> findByRecipientAndIsReadOrderByCreatedAtDesc(User recipient, boolean isRead, Pageable pageable);

    long countByRecipientAndIsReadFalse(User recipient);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient = :recipient AND n.isRead = false")
    int markAllAsReadByRecipient(@Param("recipient") User recipient);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.id = :id AND n.recipient = :recipient")
    int deleteByIdAndRecipient(@Param("id") UUID id, @Param("recipient") User recipient);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.recipient = :recipient")
    int deleteAllByRecipient(@Param("recipient") User recipient);
}
