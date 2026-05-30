package com.ratelimiter.ratelimiter.scheduler;

import com.ratelimiter.ratelimiter.repository.ApiRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// @Component makes Spring manage this class
@Component
@RequiredArgsConstructor
public class CleanupScheduler {

    private final ApiRequestRepository apiRequestRepository;

    // @Scheduled runs this method automatically on a schedule
    // cron = "0 0 0 * * *" means: at second 0, minute 0, hour 0, every day
    // that is midnight every night
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteOldRequests() {
        // delete all request logs older than 30 days
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        apiRequestRepository.deleteOlderThan(cutoff);
        System.out.println("Nightly cleanup done. Deleted records older than " + cutoff);
    }
}