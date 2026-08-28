package com.example.sclms_apigateway.filter;


import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitingFilter extends AbstractGatewayFilterFactory<RateLimitingFilter.Config> {


    private ReactiveRedisTemplate<String,String> redisTemplate;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final ConcurrentHashMap<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();

    public RateLimitingFilter() {
       super(Config.class);

        // Configure rate limiter
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .limitForPeriod(100) // 100 requests per minute
                .timeoutDuration(Duration.ofSeconds(1))
                .build();

        this.rateLimiterRegistry = RateLimiterRegistry.of(config);

    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String clientId = getClientId(exchange);
            String key = "rate-limit:" + clientId;

            return checkRateLimit(key, config)
                    .flatMap(allowed -> {
                        if (!allowed) {
                            log.warn("Rate limit exceeded for client: {}", clientId);
                            return handleRateLimitExceeded(exchange);
                        }
                        return chain.filter(exchange);
                    });
        };
    }
    private Mono<Boolean> checkRateLimit(String key, Config config) {
        int limit = config.getLimit() != null ? config.getLimit() : 100;
        Duration period = config.getPeriod() != null ? config.getPeriod() : Duration.ofMinutes(1);

        // Use Redis for distributed rate limiting
        return redisTemplate.opsForValue().increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        // First request - set expiration
                        return redisTemplate.expire(key, period)
                                .thenReturn(true);
                    }

                    // Check if under limit
                    if (count <= limit) {
                        return Mono.just(true);
                    }

                    // Rate limit exceeded
                    return Mono.just(false);
                })
                .onErrorReturn(true); // Fallback to allow request if Redis fails
    }

    private String getClientId(ServerWebExchange exchange) {
        // Try to get user ID first (if authenticated)
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (userId != null) {
            return "user:" + userId;
        }

        // Fallback to IP address
        String ip = exchange.getRequest().getRemoteAddress() != null ?
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() :
                "unknown";
        return "ip:" + ip;
    }

    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        return exchange.getResponse().setComplete();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Config {
        private Integer limit;
        private Duration period;


    }
}

