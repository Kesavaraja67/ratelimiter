package com.ratelimiter.ratelimiter.filter;

import com.ratelimiter.ratelimiter.model.ApiRequest;
import com.ratelimiter.ratelimiter.model.User;
import com.ratelimiter.ratelimiter.repository.ApiRequestRepository;
import com.ratelimiter.ratelimiter.service.RateLimitService;
import com.ratelimiter.ratelimiter.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final RateLimitService rateLimitService;
    private final ApiRequestRepository apiRequestRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // skip filter for admin endpoints
        if (requestPath.startsWith("/admin")) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader("X-API-KEY");

        if (apiKey == null || apiKey.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"API key is required\"}");
            return;
        }

        Optional<User> userOptional = userService.findByApiKey(apiKey);

        if (userOptional.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid API key\"}");
            return;
        }

        User user = userOptional.get();

        if (!user.isActive()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"User account is deactivated\"}");
            return;
        }

        boolean allowed = rateLimitService.isAllowed(
                String.valueOf(user.getId()),
                user.getMaxRequestsPerMinute()
        );

        // log this request to MySQL regardless of whether it was allowed or blocked
        logRequest(user.getId(), requestPath, allowed);

        if (!allowed) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Rate limit exceeded. Try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    // saves request record to MySQL
    private void logRequest(Long userId, String endpoint, boolean allowed) {
        ApiRequest log = new ApiRequest();
        log.setUserId(userId);
        log.setEndpoint(endpoint);
        log.setTimestamp(LocalDateTime.now());
        log.setAllowed(allowed);
        log.setAnomalyScore("NORMAL");
        apiRequestRepository.save(log);
    }
}