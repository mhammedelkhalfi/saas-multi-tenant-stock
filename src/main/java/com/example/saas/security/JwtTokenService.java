package com.example.saas.security;

import com.example.saas.exceptions.UnauthorizedException;
import com.example.saas.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenService {

    public static final String CLAIM_TENANT_ID = "tenant_id";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_TOKEN_TYPE = "token_type";
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    private final JwtProperties jwtProperties;
    private final TokenBlacklistService tokenBlacklistService;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            this.privateKey = loadPrivateKey(this.jwtProperties.getPrivateKeyPath());
            this.publicKey = loadPublicKey(this.jwtProperties.getPublicKeyPath());
            log.info("Private & Public key loaded successfully");
        } catch (final Exception e) {
            log.error("Error loading JWT keys", e);
            throw new RuntimeException("Error loading JWT keys", e);
        }
    }

    public String generateAccessToken(
            @Nonnull final String tenantId,
            @Nonnull final String userId,
            final String role
    ) {
        return buildToken(userId, tenantId, role, TOKEN_TYPE_ACCESS, this.jwtProperties.getAccessTokenExpiration());
    }

    public String generateRefreshToken(
            @Nonnull final String tenantId,
            @Nonnull final String userId,
            final String role
    ) {
        return buildToken(userId, tenantId, role, TOKEN_TYPE_REFRESH, this.jwtProperties.getRefreshTokenExpiration());
    }

    public void revokeToken(final String token) {
        final Claims claims = parseClaims(token);
        tokenBlacklistService.revoke(claims.getId(), claims.getExpiration().toInstant());
    }

    public boolean validateAccessToken(final String token) {
        return validateToken(token, TOKEN_TYPE_ACCESS);
    }

    public boolean validateRefreshToken(final String token) {
        return validateToken(token, TOKEN_TYPE_REFRESH);
    }

    public String getUserIdFromToken(final String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public String getTenantIdFromToken(final String token) {
        return getClaimsFromToken(token).get(CLAIM_TENANT_ID, String.class);
    }

    public String getRoleFromToken(final String token) {
        return getClaimsFromToken(token).get(CLAIM_ROLE, String.class);
    }

    public String getJtiFromToken(final String token) {
        return getClaimsFromToken(token).getId();
    }

    public long getAccessTokenExpirationMs() {
        return jwtProperties.getAccessTokenExpiration();
    }

    public long getRefreshTokenExpirationMs() {
        return jwtProperties.getRefreshTokenExpiration();
    }

    private String buildToken(
            final String userId,
            final String tenantId,
            final String role,
            final String tokenType,
            final long expirationMs
    ) {
        final Date now = new Date();
        final Date expiration = new Date(System.currentTimeMillis() + expirationMs);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId)
                .claim(CLAIM_TENANT_ID, tenantId)
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(now)
                .expiration(expiration)
                .issuer("stock-saas-app")
                .signWith(this.privateKey, Jwts.SIG.RS256)
                .compact();
    }

    private boolean validateToken(final String token, final String expectedType) {
        try {
            final Claims claims = parseClaims(token);
            if (tokenBlacklistService.isRevoked(claims.getId())) {
                throw new UnauthorizedException("Token has been revoked");
            }
            final String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
            if (!expectedType.equals(tokenType)) {
                throw new UnauthorizedException("Invalid token type");
            }
            return true;
        } catch (final ExpiredJwtException e) {
            throw new UnauthorizedException("Token has expired");
        } catch (final MalformedJwtException e) {
            throw new UnauthorizedException("Token is malformed");
        } catch (final SecurityException e) {
            throw new UnauthorizedException("Invalid JWT signature");
        } catch (final IllegalArgumentException e) {
            throw new UnauthorizedException("JWT claims string is empty");
        } catch (final UnauthorizedException e) {
            throw e;
        } catch (final Exception e) {
            throw new UnauthorizedException("Invalid token");
        }
    }

    private Claims getClaimsFromToken(final String token) {
        return parseClaims(token);
    }

    private Claims parseClaims(final String token) {
        return Jwts.parser()
                .verifyWith(this.publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private PrivateKey loadPrivateKey(final String privateKeyPath) throws Exception {
        try (final InputStream is = JwtTokenService.class.getClassLoader()
                .getResourceAsStream(privateKeyPath)) {

            if (is == null) {
                throw new RuntimeException("Private key not found");
            }

            final String key = new String(is.readAllBytes());
            final String privateKeyPEM = key
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            final byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
            final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        }
    }

    private PublicKey loadPublicKey(final String publicKeyPath) throws Exception {
        try (final InputStream is = JwtTokenService.class.getClassLoader()
                .getResourceAsStream(publicKeyPath)) {

            if (is == null) {
                throw new RuntimeException("Public key not found");
            }

            final String key = new String(is.readAllBytes());
            final String publicKeyPEM = key
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            final byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
            final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
            return KeyFactory.getInstance("RSA").generatePublic(keySpec);
        }
    }
}
