package com.example.saas.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.system-admin")
public class SystemAdminProperties {

    private boolean enabled = true;
    private String username = "platform-admin";
    private String password = "PlatformAdmin@123";
    private String email = "platform-admin@system.local";
    private String firstName = "Platform";
    private String lastName = "Administrator";
    private String tenantCompanyCode = "system";
    private String tenantCompanyName = "System Platform";
    private String tenantEmail = "system@platform.local";
}
