package com.example.saas.security;

import com.example.saas.config.TenantContext;
import com.example.saas.config.TenantSchemaResolver;
import com.example.saas.exceptions.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;

/**
 * Authentifie la connexion WebSocket STOMP avec le JWT (header Authorization au CONNECT).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtStompChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenService jwtTokenService;
    private final TenantSchemaResolver tenantSchemaResolver;

    @Override
    public Message<?> preSend(final Message<?> message, final MessageChannel channel) {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        final String token = resolveToken(accessor);
        if (!StringUtils.hasText(token)) {
            throw new UnauthorizedException("JWT token is required for WebSocket connection");
        }

        jwtTokenService.validateAccessToken(token);

        final String userId = jwtTokenService.getUserIdFromToken(token);
        final String tenantId = jwtTokenService.getTenantIdFromToken(token);
        final String role = jwtTokenService.getRoleFromToken(token);

        if (tenantId != null) {
            TenantContext.setCurrentTenant(tenantId);
            TenantContext.setCurrentSchema(tenantSchemaResolver.resolveTenantSchema(tenantId));
        }

        final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId,
                null,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
        accessor.setUser(authentication);

        log.debug("WebSocket CONNECT authenticated for userId={}, tenantId={}", userId, tenantId);
        return message;
    }

    @Override
    public void afterSendCompletion(
            final Message<?> message,
            final MessageChannel channel,
            final boolean sent,
            @Nullable final Exception ex
    ) {
        TenantContext.clear();
    }

    @Nullable
    private String resolveToken(final StompHeaderAccessor accessor) {
        final String authorization = accessor.getFirstNativeHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return accessor.getFirstNativeHeader("token");
    }
}
