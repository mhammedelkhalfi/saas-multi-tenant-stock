package com.example.saas.respositories;

import com.example.saas.entities.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, String> {

    Page<Notification> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(
            String userId,
            Pageable pageable
    );

    Optional<Notification> findByIdAndUserIdAndDeletedFalse(String id, String userId);

    long countByUserIdAndReadFalseAndDeletedFalse(String userId);

    @Modifying
    @Query("""
            UPDATE Notification n
            SET n.read = true, n.readAt = :readAt
            WHERE n.userId = :userId AND n.read = false AND n.deleted = false
            """)
    int markAllAsRead(@Param("userId") String userId, @Param("readAt") LocalDateTime readAt);
}
