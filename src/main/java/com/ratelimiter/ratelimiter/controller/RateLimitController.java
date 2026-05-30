package com.ratelimiter.ratelimiter.controller;

import com.ratelimiter.ratelimiter.repository.ApiRequestRepository;
import com.ratelimiter.ratelimiter.service.RateLimitService;
import com.ratelimiter.ratelimiter.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RateLimitController {

    private final RateLimitService rateLimitService;
    private final UserService userService;
    private final ApiRequestRepository apiRequestRepository;

    // protected endpoint - goes through ApiKeyFilter
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping(
            @RequestHeader("X-API-KEY") String apiKey) {

        var user = userService.findByApiKey(apiKey).get();
        long currentCount = rateLimitService.getCurrentRequestCount(
                String.valueOf(user.getId())
        );

        return ResponseEntity.ok(Map.of(
                "message", "Request allowed",
                "requestsUsedThisMinute", currentCount,
                "limitPerMinute", user.getMaxRequestsPerMinute()
        ));
    }

    // stats for a specific user - goes through ApiKeyFilter too
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            @RequestHeader("X-API-KEY") String apiKey) {

        var user = userService.findByApiKey(apiKey).get();
        Long userId = user.getId();

        long totalRequests = apiRequestRepository.countByUserIdAndTimestampAfter(
                userId, LocalDateTime.now().minusDays(7)
        );

        long blockedRequests = apiRequestRepository.countByUserIdAndAllowedFalse(userId);

        long currentWindowCount = rateLimitService.getCurrentRequestCount(
                String.valueOf(userId)
        );

        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "name", user.getName(),
                "totalRequestsLast7Days", totalRequests,
                "totalBlockedRequests", blockedRequests,
                "requestsInCurrentWindow", currentWindowCount,
                "limitPerMinute", user.getMaxRequestsPerMinute()
        ));
    }
}