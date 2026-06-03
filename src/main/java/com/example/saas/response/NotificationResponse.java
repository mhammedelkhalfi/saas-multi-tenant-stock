package com.example.saas.response;

import com.example.saas.enums.PriorityType;
import com.example.saas.enums.TypeNotification;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationResponse {
    private String id;
    private String userId;
    private String tenantId;
    private TypeNotification typeNotification;
    private String title;
    private String message;
    private String resourceType;
    private String resourceId;
    private PriorityType priority;
    private boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
