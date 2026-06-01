package com.example.saas.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String privateKeyPath;
    private String publicKeyPath;
    private long accessTokenExpiration = 900_000L;
    private long refreshTokenExpiration = 604_800_000L;
}
