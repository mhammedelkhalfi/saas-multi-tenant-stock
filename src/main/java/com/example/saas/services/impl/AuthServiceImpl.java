package com.example.saas.services.impl;

import com.example.saas.entities.User;
import com.example.saas.enums.TenantStatus;
import com.example.saas.exceptions.ForbiddenException;
import com.example.saas.exceptions.InvalidRequestException;
import com.example.saas.exceptions.UnauthorizedException;
import com.example.saas.request.LoginRequest;
import com.example.saas.response.AuthResponse;
import com.example.saas.respositories.UserRepositorie;
import com.example.saas.security.JwtCookieService;
import com.example.saas.security.JwtTokenService;
import com.example.saas.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepositorie userRepositorie;
    private final JwtTokenService jwtTokenService;
    private final JwtCookieService jwtCookieService;

    @Override
    public AuthResponse login(final LoginRequest request, final HttpServletResponse response) {
        try {
            final Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            final User user = userRepositorie.findActiveByUsername(request.getUsername())
                    .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

            validateUserAndTenant(user);

            return buildAuthResponse(user, response);
        } catch (final BadCredentialsException | DisabledException e) {
            throw new UnauthorizedException("Invalid credentials");
        }
    }

    @Override
    public AuthResponse refresh(final HttpServletRequest request, final HttpServletResponse response) {
        final String refreshToken = resolveRefreshToken(request);
        if (!StringUtils.hasText(refreshToken)) {
            throw new UnauthorizedException("Refresh token is required");
        }

        jwtTokenService.validateRefreshToken(refreshToken);

        final String userId = jwtTokenService.getUserIdFromToken(refreshToken);
        final User user = userRepositorie.findActiveById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        validateUserAndTenant(user);

        jwtTokenService.revokeToken(refreshToken);

        return buildAuthResponse(user, response);
    }

    @Override
    public void logout(final HttpServletRequest request, final HttpServletResponse response) {
        revokeIfPresent(jwtCookieService.resolveAccessToken(request));
        revokeIfPresent(jwtCookieService.resolveRefreshToken(request));
        jwtCookieService.clearAuthCookies(response);
        SecurityContextHolder.clearContext();
        log.info("User logged out successfully");
    }

    @Override
    public AuthResponse me() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("Not authenticated");
        }

        final String userId = authentication.getName();
        final User user = userRepositorie.findActiveById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .tenantId(user.getTenantId())
                .role(user.getRole().name())
                .tokenType("Bearer")
                .build();
    }

    private AuthResponse buildAuthResponse(final User user, final HttpServletResponse response) {
        final String tenantId = user.getTenantId();
        final String role = user.getRole().name();

        final String accessToken = jwtTokenService.generateAccessToken(tenantId, user.getId(), role);
        final String refreshToken = jwtTokenService.generateRefreshToken(tenantId, user.getId(), role);

        jwtCookieService.writeAccessTokenCookie(response, accessToken, jwtTokenService.getAccessTokenExpirationMs());
        jwtCookieService.writeRefreshTokenCookie(response, refreshToken, jwtTokenService.getRefreshTokenExpirationMs());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenService.getAccessTokenExpirationMs())
                .userId(user.getId())
                .username(user.getUsername())
                .tenantId(tenantId)
                .role(role)
                .build();
    }

    private void validateUserAndTenant(final User user) {
        if (!user.isEnabled() || Boolean.TRUE.equals(user.getDeleted())) {
            throw new ForbiddenException("User account is disabled");
        }
        if (user.getTenant() == null) {
            throw new InvalidRequestException("User is not linked to a tenant");
        }
        if (user.getTenant().getStatus() != TenantStatus.ACTIVE) {
            throw new ForbiddenException("Tenant is not active");
        }
    }

    private String resolveRefreshToken(final HttpServletRequest request) {
        final String fromCookie = jwtCookieService.resolveRefreshToken(request);
        if (StringUtils.hasText(fromCookie)) {
            return fromCookie;
        }
        return JwtCookieService.resolveBearerToken(request);
    }

    private void revokeIfPresent(final String token) {
        if (StringUtils.hasText(token)) {
            try {
                jwtTokenService.revokeToken(token);
            } catch (final Exception e) {
                log.debug("Could not revoke token on logout: {}", e.getMessage());
            }
        }
    }
}
