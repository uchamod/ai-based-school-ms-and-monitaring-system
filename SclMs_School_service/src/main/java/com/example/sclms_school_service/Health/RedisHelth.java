/*
package com.example.sclms_school_service.Health;



import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisHelth implements HealthIndicator {


    private final RedisTemplate<String, Object> redisTemplate;


    @Override
    public @Nullable Health health() {
        try {
            // Test Redis connection
            redisTemplate.opsForValue().get("health-check");
            return Health.up()
                    .withDetail("redis", "Connected")
                    .withDetail("status", "Available")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("redis", "Disconnected")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
*/
