package com.example.saas.request;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TenantRequest {

    private String companyName;
    private String companyCode;
    private String email;
    private String adminFullName;
    private String adminEmail;
    private String adminUsername;
    private String adminPassword;
}
