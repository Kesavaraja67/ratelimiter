package com.ratelimiter.ratelimiter.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // no two users can share the same api key
    @Column(nullable = false, unique = true)
    private String apiKey;

    @Column(nullable = false)
    private String name;

    // can be different per user, premium users get higher limits
    @Column(nullable = false)
    private int maxRequestsPerMinute;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // false means user is banned
    @Column(nullable = false)
    private boolean active;
}