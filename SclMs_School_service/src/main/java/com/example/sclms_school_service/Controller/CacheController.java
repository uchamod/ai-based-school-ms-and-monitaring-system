package com.example.sclms_school_service.Controller;

import com.example.sclms_school_service.Service.SchoolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Slf4j
public class CacheController {

    private final CacheManager cacheManager;
    private final SchoolService schoolService;

    /**
     * Get cache statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        // Get cache names
        Collection<String> cacheNames = new HashSet<>();
        if (cacheManager instanceof org.springframework.data.redis.cache.RedisCacheManager) {
            cacheNames = (cacheManager)
                    .getCacheNames();
        }

        stats.put("cacheNames", cacheNames);
        stats.put("cacheManager", cacheManager.getClass().getSimpleName());
        stats.put("timestamp", new Date());

        return ResponseEntity.ok(stats);
    }

    /**
     * Clear specific cache
     */
    @DeleteMapping("/clear/{cacheName}")
    public ResponseEntity<String> clearCache(@PathVariable String cacheName) {
        try {
            CacheManager cacheManager = this.cacheManager;
            if (cacheManager != null) {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    log.info("Cache cleared: {}", cacheName);
                    return ResponseEntity.ok("Cache cleared successfully: " + cacheName);
                }
            }
            return ResponseEntity.badRequest().body("Cache not found: " + cacheName);
        } catch (Exception e) {
            log.error("Error clearing cache: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Failed to clear cache: " + e.getMessage());
        }
    }

    /**
     * Clear all caches
     */
    @DeleteMapping("/clear-all")
    public ResponseEntity<String> clearAllCaches() {
        try {
            schoolService.evictAllSchoolCaches();
            log.info("All school caches cleared");
            return ResponseEntity.ok("All school caches cleared successfully");
        } catch (Exception e) {
            log.error("Error clearing all caches: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Failed to clear all caches: " + e.getMessage());
        }
    }

    /**
     * Evict specific school from cache
     */
    @DeleteMapping("/evict-school/{id}")
    public ResponseEntity<String> evictSchoolCache(@PathVariable UUID id) {
        try {
            schoolService.evictSchoolCache(id);
            log.info("School cache evicted: {}", id);
            return ResponseEntity.ok("School cache evicted successfully: " + id);
        } catch (Exception e) {
            log.error("Error evicting school cache: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Failed to evict school cache: " + e.getMessage());
        }
    }
}
