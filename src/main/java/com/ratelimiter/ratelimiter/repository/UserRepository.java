package com.ratelimiter.ratelimiter.repository;

import com.ratelimiter.ratelimiter.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // SELECT * FROM users WHERE api_key = ?
    Optional<User> findByApiKey(String apiKey);

    // SELECT * FROM users WHERE name = ?
    Optional<User> findByName(String name);

    // checks if api key already exists before creating a new user
    boolean existsByApiKey(String apiKey);
}