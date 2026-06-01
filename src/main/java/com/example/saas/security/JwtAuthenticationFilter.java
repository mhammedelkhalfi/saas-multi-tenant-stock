package com.example.saas.security;

import com.example.saas.config.TenantContext;
import com.example.saas.config.TenantSchemaResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final List<String> PUBLIC_AUTH_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/refresh"
    );

    private final JwtTokenService jwtTokenService;
    private final JwtCookieService jwtCookieService;
    private final TenantSchemaResolver tenantSchemaResolver;

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain
    ) throws ServletException, IOException {

        if (isPublicAuthPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = jwtCookieService.resolveAccessToken(request);
            if (StringUtils.hasText(jwt) && jwtTokenService.validateAccessToken(jwt)) {
                authenticateRequest(request, jwt);
            }
        } catch (final Exception e) {
            log.debug("JWT authentication failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private void authenticateRequest(final HttpServletRequest request, final String jwt) {
        final String userId = jwtTokenService.getUserIdFromToken(jwt);
        final String tenantId = jwtTokenService.getTenantIdFromToken(jwt);
        final String role = jwtTokenService.getRoleFromToken(jwt);

        if (tenantId != null) {
            TenantContext.setCurrentTenant(tenantId);
            final String schemaName = tenantSchemaResolver.resolveTenantSchema(tenantId);
            TenantContext.setCurrentSchema(schemaName);
        }

        final UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(role))
                );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("Authenticated userId={}, tenant={}, role={}", userId, tenantId, role);
    }

    private boolean isPublicAuthPath(final String uri) {
        return PUBLIC_AUTH_PATHS.stream().anyMatch(uri::startsWith);
    }
}
