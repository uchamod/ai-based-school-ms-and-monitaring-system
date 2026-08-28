package com.example.sclms_apigateway.util;

import com.example.sclms_apigateway.Model.TokencachedData;
import com.example.sclms_apigateway.Service.GatewayCacheService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    private final  GatewayCacheService gatewayCacheService;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).get("userId", String.class);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractAllClaims(token).getExpiration().before(new java.util.Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Enhanced token validation with cache
     */
    public Mono<Boolean> validateTokenWithCache(String token) {
        return gatewayCacheService.validateToken(token)
                .flatMap(isCached -> {
                    if (isCached) {
                        // Token is valid and cached
                        return Mono.just(true);
                    }

                    // Token not in cache - validate JWT and cache it
                    try {
                        if (isTokenValid(token)) {
                            String email = extractEmail(token);
                            String userId = extractUserId(token);
                            String role = extractRole(token);

                            // Cache the token
                            long ttlSeconds = calculateRemainingTTL(token);
                            return gatewayCacheService.cacheToken(token, userId, email, role,
                                            Duration.ofSeconds(ttlSeconds))
                                    .thenReturn(true);
                        }
                        return Mono.just(false);
                    } catch (Exception e) {
                        log.error("Token validation error: {}", e.getMessage());
                        return Mono.just(false);
                    }
                });
    }
    /**
     * Calculate remaining TTL of token
     */
    public long calculateRemainingTTL(String token) {
        try {
            Date expiration = extractExpiration(token);
            long now = System.currentTimeMillis();
            long remaining = expiration.getTime() - now;
            return remaining > 0 ? remaining / 1000 : 0;
        } catch (Exception e) {
            return expiration / 1000; // Default TTL
        }
    }

    /**
     * Get cached token data
     */
    public Mono<TokencachedData> getCachedTokenData(String token) {
        return gatewayCacheService.getTokenData(token);
    }

    /**
     * Revoke token (logout)
     */
    public Mono<Boolean> revokeToken(String token) {
        return gatewayCacheService.revokeToken(token);
    }
}
