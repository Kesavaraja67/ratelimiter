package com.ratelimiter.ratelimiter.repository;

import com.ratelimiter.ratelimiter.model.ApiRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApiRequestRepository extends JpaRepository<ApiRequest, Long> {

    // all requests by a specific user
    List<ApiRequest> findByUserId(Long userId);

    // count how many requests a user made after a certain time
    // used for anomaly detection baseline calculation
    long countByUserIdAndTimestampAfter(Long userId, LocalDateTime after);

    // count how many were blocked for a user
    long countByUserIdAndAllowedFalse(Long userId);

    // delete records older than a given time - used by nightly cleanup job
    @Query("DELETE FROM ApiRequest a WHERE a.timestamp < :cutoff")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteOlderThan(@Param("cutoff") LocalDateTime cutoff);
}