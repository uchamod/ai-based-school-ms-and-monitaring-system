package com.example.sclms_apigateway.filter;

import com.example.sclms_apigateway.Model.TokencachedData;
import com.example.sclms_apigateway.Service.GatewayCacheService;
import com.example.sclms_apigateway.exception.UnauthorizedException;
import com.example.sclms_apigateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    private final GatewayCacheService gatewayCacheService;

    public AuthenticationFilter(JwtUtil jwtUtil, GatewayCacheService gatewayCacheService) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.gatewayCacheService = gatewayCacheService;
    }


    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/isexist",
            "/actuator/health",
            "/swagger-ui",
            "/v3/api-docs",
            "/api/authoption/logout"

    );

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Skip public endpoints
            if (isPublicEndpoint(exchange.getRequest())) {
                return chain.filter(exchange);
            }
            ServerHttpRequest request = exchange.getRequest();

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("Invalid Authorization header format");
                return Mono.error(new UnauthorizedException("Missing or invalid Authorization header"));
            }

            String token = authHeader.substring(7);


            // Validate token with cache
            return jwtUtil.validateTokenWithCache(token)
                    .flatMap(valid -> {
                        if (!valid) {
                            log.warn("Invalid or expired token");
                            return Unauthorized(exchange, "Invalid or expired token");
                        }

                        // Get token data from cache
                        return jwtUtil.getCachedTokenData(token)
                                .<TokencachedData>flatMap(tokenData -> {
                                    if (tokenData == null) {
                                        // Fallback: extract from JWT
                                        try {
                                            String email = jwtUtil.extractEmail(token);
                                            String userId = jwtUtil.extractUserId(token);
                                            String role = jwtUtil.extractRole(token);
                                            TokencachedData cachedData =
                                                    new TokencachedData(userId, email, role);
                                            // Cache for future requests
                                            return gatewayCacheService.cacheToken(token, userId, email, role,
                                                            java.time.Duration.ofSeconds(jwtUtil.calculateRemainingTTL(token)))
                                                    .thenReturn(cachedData);
                                        } catch (Exception e) {
                                            log.error("Error extracting token data", e);
                                            return handleUnauthorized(exchange, "Invalid token format");

                                        }
                                    }
                                    return Mono.just(tokenData);
                                })
                                .flatMap(tokenData -> {
                                    // Add user context to request headers
                                    ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                            .header("X-User-Email", tokenData.getEmail())
                                            .header("X-User-Id", tokenData.getUserId())
                                            .header("X-User-Role", tokenData.getRole())
                                            .build();

                                    // Also pass the token for service-to-service communication
                                    ServerHttpRequest finalRequest = modifiedRequest.mutate()
                                            .header("X-Auth-Token", token)
                                            .build();

                                    log.debug("Request authenticated for user: {}", tokenData.getUserId());
                                    return chain.filter(exchange.mutate().request(finalRequest).build());
                                });
                    });
        };
    }
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return apply(new Config()).filter(exchange, chain);
    }

    @Override
    public int getOrder() {
        return -100; // Highest priority
    }

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private Mono<TokencachedData> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete().then(Mono.empty());
    }
    private Mono<Void> Unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

//.then(Mono.empty())
    public static class Config {
    }
}
