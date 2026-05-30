package com.ratelimiter.ratelimiter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.rate-limit.max-requests-per-minute:100}")
    private int defaultMaxRequests;

    @Value("${app.rate-limit.window-size-seconds:60}")
    private int windowSizeSeconds;

    // returns true if allowed, false if blocked
    public boolean isAllowed(String userId, int maxRequestsPerMinute) {
        String redisKey = "rate_limit:" + userId;
        long currentTime = Instant.now().toEpochMilli();
        long windowStart = currentTime - (windowSizeSeconds * 1000L);

        var ops = redisTemplate.opsForZSet();

        // remove requests outside the 60 second window
        ops.removeRangeByScore(redisKey, 0, windowStart);

        // count requests inside the window
        Long requestCount = ops.zCard(redisKey);
        long count = requestCount == null ? 0 : requestCount;

        if (count < maxRequestsPerMinute) {
            // add this request with current timestamp as score
            ops.add(redisKey, currentTime + "-" + Math.random(), currentTime);
            redisTemplate.expire(redisKey, 70, TimeUnit.SECONDS);
            return true;
        }

        return false;
    }

    // how many requests has this user made in the current window
    public long getCurrentRequestCount(String userId) {
        String redisKey = "rate_limit:" + userId;
        long windowStart = Instant.now().toEpochMilli() - (windowSizeSeconds * 1000L);

        var ops = redisTemplate.opsForZSet();
        ops.removeRangeByScore(redisKey, 0, windowStart);

        Long count = ops.zCard(redisKey);
        return count == null ? 0 : count;
    }
}