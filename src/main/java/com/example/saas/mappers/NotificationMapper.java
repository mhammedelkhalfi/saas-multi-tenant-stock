package com.example.saas.mappers;

import com.example.saas.entities.Notification;
import com.example.saas.enums.PriorityType;
import com.example.saas.request.NotificationRequest;
import com.example.saas.response.NotificationResponse;
import org.springframework.stereotype.Service;

@Service
public class NotificationMapper {

    public Notification toEntity(final NotificationRequest request) {
        return Notification.builder()
                .userId(request.getUserId())
                .tenantId(request.getTenantId())
                .typeNotification(request.getTypeNotification())
                .title(request.getTitle())
                .message(request.getMessage())
                .resourceType(request.getResourceType())
                .resourceId(request.getResourceId())
                .priority(request.getPriority() != null ? request.getPriority() : PriorityType.MEDIUM)
                .read(false)
                .deleted(false)
                .build();
    }

    public NotificationResponse toResponse(final Notification entity) {
        return NotificationResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .tenantId(entity.getTenantId())
                .typeNotification(entity.getTypeNotification())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .resourceType(entity.getResourceType())
                .resourceId(entity.getResourceId())
                .priority(entity.getPriority())
                .read(entity.isRead())
                .readAt(entity.getReadAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
