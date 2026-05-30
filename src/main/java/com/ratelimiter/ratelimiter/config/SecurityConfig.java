package com.ratelimiter.ratelimiter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

// @Configuration tells Spring this class contains setup instructions
// Spring runs all @Bean methods in this class at startup
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // permitAll means allow every request through without authentication
                // we will replace this later with our API key filter
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}