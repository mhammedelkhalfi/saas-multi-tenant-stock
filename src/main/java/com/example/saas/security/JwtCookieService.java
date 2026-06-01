package com.example.saas.security;

import com.example.saas.properties.SecurityProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class JwtCookieService {

    private final SecurityProperties securityProperties;

    public void writeAccessTokenCookie(final HttpServletResponse response, final String token, final long maxAgeMs) {
        response.addHeader("Set-Cookie", buildCookie(
                securityProperties.getCookie().getAccessTokenName(),
                token,
                maxAgeMs
        ).toString());
    }

    public void writeRefreshTokenCookie(final HttpServletResponse response, final String token, final long maxAgeMs) {
        response.addHeader("Set-Cookie", buildCookie(
                securityProperties.getCookie().getRefreshTokenName(),
                token,
                maxAgeMs
        ).toString());
    }

    public void clearAuthCookies(final HttpServletResponse response) {
        writeAccessTokenCookie(response, "", 0);
        writeRefreshTokenCookie(response, "", 0);
    }

    public String resolveAccessToken(final HttpServletRequest request) {
        final String fromCookie = readCookie(request, securityProperties.getCookie().getAccessTokenName());
        if (StringUtils.hasText(fromCookie)) {
            return fromCookie;
        }
        return resolveBearerToken(request);
    }

    public String resolveRefreshToken(final HttpServletRequest request) {
        return readCookie(request, securityProperties.getCookie().getRefreshTokenName());
    }

    public static String resolveBearerToken(final HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    private ResponseCookie buildCookie(final String name, final String value, final long maxAgeMs) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(securityProperties.getCookie().isSecure())
                .sameSite(securityProperties.getCookie().getSameSite())
                .path(securityProperties.getCookie().getPath())
                .maxAge(Duration.ofMillis(maxAgeMs))
                .build();
    }

    private String readCookie(final HttpServletRequest request, final String name) {
        final Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (final Cookie cookie : cookies) {
            if (name.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
