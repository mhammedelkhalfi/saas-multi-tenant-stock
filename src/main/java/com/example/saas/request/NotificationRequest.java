package com.example.saas.request;

import com.example.saas.enums.PriorityType;
import com.example.saas.enums.TypeNotification;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationRequest {
    private String userId;
    private String tenantId;
    private TypeNotification typeNotification;
    private String title;
    private String message;
    private String resourceType;
    private String resourceId;
    private PriorityType priority;
}
