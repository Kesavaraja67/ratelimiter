package com.ratelimiter.ratelimiter.controller;

import com.ratelimiter.ratelimiter.model.User;
import com.ratelimiter.ratelimiter.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    // POST http://localhost:8080/admin/users
    // body: { "name": "John", "maxRequestsPerMinute": 100 }
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        User newUser = userService.createUser(
                request.getName(),
                request.getMaxRequestsPerMinute()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    // PUT http://localhost:8080/admin/users/1
    @PutMapping("/users/{id}")
    public ResponseEntity<String> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok("User deactivated successfully");
    }

    static class CreateUserRequest {
        private String name;
        private int maxRequestsPerMinute;

        public String getName() { return name; }
        public int getMaxRequestsPerMinute() { return maxRequestsPerMinute; }
    }
}