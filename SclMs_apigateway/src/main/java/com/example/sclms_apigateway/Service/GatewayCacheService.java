package com.example.sclms_apigateway.Service;

import com.example.sclms_apigateway.Model.TokencachedData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class GatewayCacheService {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    private static final String TOKEN_PREFIX = "jwt:token:";
    private static final String TOKEN_BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String USER_SESSION_PREFIX = "user:session:";
    private static final String USER_PERMISSIONS_PREFIX = "user:permissions:";


    /*public GatewayCacheService(ReactiveRedisTemplate<String, Object> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }*/

    /**
     * Hash token for cache key (to avoid storing full token as key)
     */
    private String hashToken(String token) {
        // Use a simple hash or you can use MessageDigest for SHA-256
        return Integer.toHexString(token.hashCode());
    }
    /**
     * Cache token with user context
     */
    public Mono<Void> cacheToken(String token, String userId, String email, String role, Duration ttl) {
        String tokenKey = TOKEN_PREFIX + hashToken(token);
        String userKey = USER_SESSION_PREFIX + userId;

        // Store token -> user mapping
        String tokenData = userId + ":" + email + ":" + role;

        return reactiveRedisTemplate.opsForValue()
                .set(tokenKey, tokenData, ttl)
                .then(reactiveRedisTemplate.opsForValue()
                        .set(userKey, token, Duration.ofMinutes(30))) // Session TTL
                .doOnSuccess(v -> log.debug("Token cached for user: {}", userId))
                .doOnError(e -> log.error("Failed to cache token: {}", e.getMessage()))
                .then();
    }
    /**
     * Get cached token data
     */
    public Mono<TokencachedData> getTokenData(String token) {
        String tokenKey = TOKEN_PREFIX + hashToken(token);

        return reactiveRedisTemplate.opsForValue()
                .get(tokenKey)
                .map(data -> {
                    String[] parts = data.toString().split(":");
                    if (parts.length == 3) {
                        return new TokencachedData(parts[0], parts[1], parts[2]);
                    }
                    return null;
                })
                .doOnSuccess(data -> {
                    if (data != null) {
                        log.debug("Token found in cache for user: {}", data.getUserId());
                    }
                });
    }

    /**
     * Validate token - checks cache and blacklist
     */
    public Mono<Boolean> validateToken(String token) {
        String tokenKey = TOKEN_PREFIX + hashToken(token);
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + hashToken(token);

        // Check if token is blacklisted first (fastest check)
        return reactiveRedisTemplate.hasKey(blacklistKey)
                .flatMap(isBlacklisted -> {
                    if (isBlacklisted) {
                        log.debug("Token is blacklisted");
                        return Mono.just(false);
                    }
                    // Check if token exists in cache
                    return reactiveRedisTemplate.hasKey(tokenKey);
                })
                .doOnSuccess(valid -> {
                    if (!valid) {
                        log.debug("Token validation failed");
                    }
                });
    }

    /**
     * Revoke token (logout)
     */
    public Mono<Boolean> revokeToken(String token) {
        String tokenKey = TOKEN_PREFIX + hashToken(token);
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + hashToken(token);

        // Get user info before deleting
        return getTokenData(token)
                .flatMap(tokenData -> {
                    // Add to blacklist
                    return reactiveRedisTemplate.opsForValue()
                            .set(blacklistKey, "revoked", Duration.ofMinutes(30)) // Keep blacklist for remaining TTL
                            .then(reactiveRedisTemplate.delete(tokenKey))
                            .then(reactiveRedisTemplate.delete(USER_SESSION_PREFIX + tokenData.getUserId()))
                            .then(reactiveRedisTemplate.delete(USER_PERMISSIONS_PREFIX + tokenData.getUserId()))
                            .thenReturn(true);
                })
                .switchIfEmpty(Mono.just(false))
                .doOnSuccess(success -> {
                    if (success) {
                        log.info("Token revoked successfully");
                    }
                });
    }

    /**
     * Cache user permissions
     */
    public Mono<Void> cacheUserPermissions(String userId, String permissions, Duration ttl) {
        String key = USER_PERMISSIONS_PREFIX + userId;
        return reactiveRedisTemplate.opsForValue()
                .set(key, permissions, ttl)
                .doOnSuccess(v -> log.debug("Permissions cached for user: {}", userId))
                .then();
    }

    /**
     * Get cached user permissions
     */
    public Mono<String> getUserPermissions(String userId) {
        String key = USER_PERMISSIONS_PREFIX + userId;
        return reactiveRedisTemplate.opsForValue()
                .get(key)
                .doOnSuccess(perms  -> {
                    if (perms != null) {
                        log.debug("Permissions found in cache for user: {}", userId);
                    }
                });
    }
}
