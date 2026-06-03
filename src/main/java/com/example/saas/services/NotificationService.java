package com.example.saas.services;

import com.example.saas.common.PageResponse;
import com.example.saas.request.NotificationRequest;
import com.example.saas.response.NotificationResponse;

public interface NotificationService {

    void sendToUser(String userId, NotificationRequest request);

    void sendToTenant(String tenantId, NotificationRequest request);

    PageResponse<NotificationResponse> getUserNotifications(String userId, int page, int size);

    void markAsRead(String notificationId, String userId);

    void markAllAsRead(String userId);

    long countUnread(String userId);

    void deleteNotification(String notificationId, String userId);
}
