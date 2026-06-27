package com.challengeteam.shop.security.filter;

import com.challengeteam.shop.properties.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final Map<String, String> RATE_LIMITED_ENDPOINTS = Map.of(
            "/api/auth/login", "login",
            "/api/auth/refresh-token", "refresh-token",
            "/api/auth/forgot-password", "forgot-password",
            "/api/auth/reset-password", "reset-password"
    );

    private final RateLimitProperties properties;
    private final Clock clock = Clock.systemUTC();
    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !properties.isEnabled()
                || !HttpMethod.POST.matches(request.getMethod())
                || !RATE_LIMITED_ENDPOINTS.containsKey(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        long now = clock.millis();
        long windowMillis = properties.getWindow().toMillis();
        String key = clientIp(request) + ':' + RATE_LIMITED_ENDPOINTS.get(request.getRequestURI());

        WindowCounter counter = counters.compute(key, (ignored, current) -> {
            if (current == null || now >= current.windowStartedAt + windowMillis) {
                return new WindowCounter(now, 1);
            }
            current.requestCount++;
            return current;
        });

        if (counter.requestCount > properties.getRequestsPerWindow()) {
            long retryAfterSeconds = Math.max(
                    1,
                    (counter.windowStartedAt + windowMillis - now + 999) / 1000);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", Long.toString(retryAfterSeconds));
            response.getWriter().write("{\"error\":\"Too many requests\"}");
            return;
        }

        cleanupExpiredCounters(now, windowMillis);
        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",", 2)[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void cleanupExpiredCounters(long now, long windowMillis) {
        counters.entrySet().removeIf(entry -> now >= entry.getValue().windowStartedAt + windowMillis);
    }

    private static class WindowCounter {
        private final long windowStartedAt;
        private int requestCount;

        private WindowCounter(long windowStartedAt, int requestCount) {
            this.windowStartedAt = windowStartedAt;
            this.requestCount = requestCount;
        }
    }
}
