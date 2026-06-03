package com.example.saas.services.impl;

import com.example.saas.common.PageResponse;
import com.example.saas.entities.Notification;
import com.example.saas.entities.User;
import com.example.saas.exceptions.ResourceNotFoundException;
import com.example.saas.mappers.NotificationMapper;
import com.example.saas.notification.NotificationDestination;
import com.example.saas.request.NotificationRequest;
import com.example.saas.response.NotificationResponse;
import com.example.saas.respositories.NotificationRepository;
import com.example.saas.respositories.UserRepositorie;
import com.example.saas.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepositorie userRepositorie;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendToUser(final String userId, final NotificationRequest request) {
        request.setUserId(userId);
        if (request.getTenantId() == null) {
            userRepositorie.findActiveById(userId)
                    .map(User::getTenantId)
                    .ifPresent(request::setTenantId);
        }
        persistAndPush(notificationMapper.toEntity(request));
        log.debug("Notification sent to user {}", userId);
    }

    @Override
    public void sendToTenant(final String tenantId, final NotificationRequest request) {
        request.setTenantId(tenantId);
        final List<User> users = userRepositorie.findAllActiveByTenantId(tenantId);
        for (final User user : users) {
            sendToUser(user.getId(), request);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getUserNotifications(
            final String userId,
            final int page,
            final int size
    ) {
        return PageResponse.of(
                notificationRepository
                        .findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                        .map(notificationMapper::toResponse)
        );
    }

    @Override
    public void markAsRead(final String notificationId, final String userId) {
        final Notification notification = notificationRepository
                .findByIdAndUserIdAndDeletedFalse(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(final String userId) {
        notificationRepository.markAllAsRead(userId, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(final String userId) {
        return notificationRepository.countByUserIdAndReadFalseAndDeletedFalse(userId);
    }

    @Override
    public void deleteNotification(final String notificationId, final String userId) {
        final Notification notification = notificationRepository
                .findByIdAndUserIdAndDeletedFalse(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setDeleted(true);
        notificationRepository.save(notification);
    }

    /**
     * Sauvegarde en base puis envoie en temps réel via WebSocket (STOMP).
     */
    private void persistAndPush(final Notification notification) {
        final Notification saved = notificationRepository.save(notification);
        final NotificationResponse response = notificationMapper.toResponse(saved);

        messagingTemplate.convertAndSendToUser(
                saved.getUserId(),
                NotificationDestination.USER_QUEUE,
                response
        );

        messagingTemplate.convertAndSend(
                NotificationDestination.tenantTopic(saved.getTenantId()),
                response
        );
    }
}
