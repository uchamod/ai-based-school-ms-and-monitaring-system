package com.example.sclms_apigateway.Controller;

import com.example.sclms_apigateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private JwtUtil jwtUtil;

    @PostMapping("/logout")
    public Mono<ResponseEntity<String>> logout(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid token"));
        }

        String token = authHeader.substring(7);

        return jwtUtil.revokeToken(token)
                .map(success -> {
                    if (success) {
                        log.info("User logged out successfully");
                        return ResponseEntity.ok("Logged out successfully");
                    } else {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Logout failed");
                    }
                });
    }
}
