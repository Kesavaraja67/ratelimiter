package com.ratelimiter.ratelimiter.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // which user made this request
    @Column(nullable = false)
    private Long userId;

    // which endpoint they hit
    @Column(nullable = false)
    private String endpoint;

    // when it happened
    @Column(nullable = false)
    private LocalDateTime timestamp;

    // true = request went through, false = was blocked
    @Column(nullable = false)
    private boolean allowed;

    // LOW, MEDIUM, HIGH or NORMAL
    @Column(nullable = false)
    private String anomalyScore;
}