package com.example.saas.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final ConcurrentHashMap<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    public void revoke(final String jti, final Instant expiresAt) {
        if (jti != null && expiresAt != null) {
            revokedTokens.put(jti, expiresAt);
        }
    }

    public boolean isRevoked(final String jti) {
        if (jti == null) {
            return false;
        }
        final Instant expiresAt = revokedTokens.get(jti);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt.isBefore(Instant.now())) {
            revokedTokens.remove(jti);
            return false;
        }
        return true;
    }

    @Scheduled(fixedRate = 3_600_000)
    public void cleanupExpiredEntries() {
        final Instant now = Instant.now();
        final Iterator<Map.Entry<String, Instant>> iterator = revokedTokens.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().isBefore(now)) {
                iterator.remove();
            }
        }
    }
}
