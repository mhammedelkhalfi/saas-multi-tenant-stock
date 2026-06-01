package com.example.saas.response;

import com.example.saas.enums.TenantStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TenantResponse {

    private String tenantId;
    private String companyName;
    private String companyCode;
    private String email;
    private String adminFullName;
    private String adminEmail;
    private String adminUsername;
    private LocalDateTime createdAt;
    private TenantStatus status;

}
