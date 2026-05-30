package com.ratelimiter.ratelimiter.service;

import com.ratelimiter.ratelimiter.model.User;
import com.ratelimiter.ratelimiter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // creates user with a randomly generated api key
    public User createUser(String name, int maxRequestsPerMinute) {
        String apiKey = UUID.randomUUID().toString();

        User user = new User();
        user.setName(name);
        user.setApiKey(apiKey);
        user.setMaxRequestsPerMinute(maxRequestsPerMinute);
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);

        return userRepository.save(user);
    }

    public Optional<User> findByApiKey(String apiKey) {
        return userRepository.findByApiKey(apiKey);
    }

    // used by the filter to validate every incoming request
    public boolean isValidApiKey(String apiKey) {
        return userRepository.existsByApiKey(apiKey);
    }

    public boolean isActiveUser(String apiKey) {
        Optional<User> user = userRepository.findByApiKey(apiKey);
        return user.isPresent() && user.get().isActive();
    }

    public void deactivateUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            user.get().setActive(false);
            userRepository.save(user.get());
        }
    }
}