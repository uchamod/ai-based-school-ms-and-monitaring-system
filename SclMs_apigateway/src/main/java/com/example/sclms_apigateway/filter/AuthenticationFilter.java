package com.example.sclms_apigateway.filter;

import com.example.sclms_apigateway.exception.UnauthorizedException;
import com.example.sclms_apigateway.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;

    public AuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("Invalid Authorization header format");
                return Mono.error(new UnauthorizedException("Missing or invalid Authorization header"));
            }

            String token = authHeader.substring(7);

            if (!jwtUtil.isTokenValid(token)) {
                System.out.println("Invalid or expired JWT token");
                return Mono.error(new UnauthorizedException("Invalid or expired JWT token"));
            }
            try{
                String email = jwtUtil.extractEmail(token);
                String userId = jwtUtil.extractUserId(token);
                String role = jwtUtil.extractRole(token);

                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Email", email)
                        .header("X-User-Id", userId)
                        .header("X-User-Role", role)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            }catch(Exception e){
                System.out.println("Error processing JWT token: " + e.getMessage());
                return Mono.error(new UnauthorizedException("Error processing JWT token"));
            }

        };
    }

    public static class Config {
    }
}
