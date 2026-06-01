package com.example.saas.security;

import com.example.saas.properties.SecurityProperties;
import com.example.saas.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;
    private final ObjectMapper objectMapper;
    private final Map<String, Deque<Long>> requestTimestamps = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        final String uri = request.getRequestURI();
        return !uri.startsWith("/api/v1/auth/login") && !uri.startsWith("/api/v1/auth/refresh");
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain
    ) throws ServletException, IOException {

        final String clientKey = resolveClientKey(request);
        if (isRateLimited(clientKey)) {
            writeTooManyRequests(response, request.getRequestURI());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(final String clientKey) {
        final long windowMs = securityProperties.getRateLimit().getAuthWindowSeconds() * 1000L;
        final int maxRequests = securityProperties.getRateLimit().getAuthMaxRequests();
        final long now = System.currentTimeMillis();

        final Deque<Long> timestamps = requestTimestamps.computeIfAbsent(clientKey, key -> new java.util.ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < now - windowMs) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= maxRequests) {
                return true;
            }
            timestamps.addLast(now);
        }
        return false;
    }

    private String resolveClientKey(final HttpServletRequest request) {
        final String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeTooManyRequests(final HttpServletResponse response, final String path) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        final ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .code("RATE_LIMIT_EXCEEDED")
                .message("Too many authentication attempts. Please try again later.")
                .path(path)
                .build();

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
