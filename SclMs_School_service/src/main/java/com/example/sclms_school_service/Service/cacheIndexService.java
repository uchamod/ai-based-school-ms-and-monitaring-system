package com.example.sclms_school_service.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class cacheIndexService {

    private final StringRedisTemplate redisTemplate;
    private final CacheManager cacheManager;

    /** Must exceed the longest entry TTL (schools = 30 min) so indexes outlive the keys they track. */
    private static final Duration INDEX_TTL = Duration.ofMinutes(45);

    @Async
    public void trackForUser(String userId, String cacheKey) {
        if (userId == null || cacheKey == null) return;
        redisTemplate.opsForSet().add("user-index::" + userId, cacheKey);
        redisTemplate.expire("user-index::" + userId, INDEX_TTL);

        System.out.println("Tracked key {} for user {}"+cacheKey + " "+userId);
    }
    @Async
    public void trackForSchool(UUID schoolId, String cacheKey) {
        if (schoolId == null || cacheKey == null) return;
        redisTemplate.opsForSet().add("school-index::" + schoolId, cacheKey);
        redisTemplate.expire("school-index::" + schoolId, INDEX_TTL);
        System.out.println("Tracked key {} for school {}"+cacheKey + " "+schoolId);
    }

    @Async
    public void invalidateSchoolEverywhere(UUID schoolId) {
        String idx = "school-index::" + schoolId;
        Set<String> keys = redisTemplate.opsForSet().members(idx);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);       // pipelined DEL
            redisTemplate.delete(idx);
            System.out.println("Invalidated {} cache entries for school {}"+ keys.size() + " "+schoolId);
        }else{
            System.out.println("No cache entries found for school {}"+schoolId);
        }
        redisTemplate.delete(idx);
    }

    @Async
    public void invalidateUser(String userId) {
        String idx = "user-index::" + userId;
        Set<String> keys = redisTemplate.opsForSet().members(idx);
        if (keys != null && !keys.isEmpty()){
            redisTemplate.delete(keys);
            System.out.println("Invalidated {} cache entries for user {}"+ keys.size() + " "+userId);
        }
        redisTemplate.delete(idx);
    }
    /**
     * Force eviction of a single cache key (sync, for immediate use)
     */
    public void evictSingleKey(String cacheKey) {
        if (cacheKey != null) {
            redisTemplate.delete(cacheKey);
            System.out.println("Evicted single key: {}"+cacheKey);
        }
    }

    /** Bulk clear by cache name — only for rare "everything changed" events (e.g. a new school was inserted). */
    @Async
    public void evictCacheNames(String... cacheNames) {
        try {
            for (String name : cacheNames) {
                Cache cache = cacheManager.getCache(name);
                if (cache != null) cache.clear();
            }
        } catch (Exception e) {
            System.out.println("Evicted single key: {}"+cacheNames);

        }
    }

    /**
     * Runs the action only after the current transaction commits. Passing a call to an
     * @Async method through the injected proxy here keeps it async AND after-commit.
     * If no transaction is active, it runs immediately.
     */
    public void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

}
