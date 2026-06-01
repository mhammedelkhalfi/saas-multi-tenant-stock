package com.example.saas.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private Cookie cookie = new Cookie();
    private RateLimit rateLimit = new RateLimit();

    @Getter
    @Setter
    public static class Cookie {
        private boolean secure = false;
        private String sameSite = "Lax";
        private String accessTokenName = "access_token";
        private String refreshTokenName = "refresh_token";
        private String path = "/";
    }

    @Getter
    @Setter
    public static class RateLimit {
        private int authMaxRequests = 10;
        private int authWindowSeconds = 60;
    }
}
