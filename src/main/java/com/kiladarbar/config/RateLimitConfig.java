package com.kiladarbar.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory rate limiter using Bucket4j.
 * For distributed deployments, replace ConcurrentHashMap with Redis-backed storage.
 *
 * Limits:
 *   /auth/send-otp  → 5 req / 60s  per IP
 *   /auth/**        → 20 req / 60s  per IP
 *   default         → 200 req / 60s per IP
 */
@Configuration
@Slf4j
public class RateLimitConfig extends OncePerRequestFilter {

    private final Map<String, Bucket> otpBuckets     = new ConcurrentHashMap<>();
    private final Map<String, Bucket> authBuckets    = new ConcurrentHashMap<>();
    private final Map<String, Bucket> defaultBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String ip   = getClientIp(req);
        String path = req.getRequestURI();

        Bucket bucket = resolveBucket(ip, path);

        if (bucket.tryConsume(1)) {
            chain.doFilter(req, res);
        } else {
            log.warn("Rate limit exceeded: ip={} path={}", ip, path);
            res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            res.setContentType("application/json");
            res.getWriter().write("""
                    {"success":false,"message":"Too many requests. Please slow down."}
                    """);
        }
    }

    private Bucket resolveBucket(String ip, String path) {
        if (path.contains("/auth/send-otp")) {
            return otpBuckets.computeIfAbsent(ip, k -> buildBucket(5, 60));
        } else if (path.startsWith("/api/v1/auth")) {
            return authBuckets.computeIfAbsent(ip, k -> buildBucket(20, 60));
        } else {
            return defaultBuckets.computeIfAbsent(ip, k -> buildBucket(200, 60));
        }
    }

    private Bucket buildBucket(long capacity, long refillSeconds) {
        Bandwidth limit = Bandwidth.classic(capacity,
                Refill.intervally(capacity, Duration.ofSeconds(refillSeconds)));
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        return (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : req.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String path = req.getRequestURI();
        return path.startsWith("/actuator") || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui");
    }
}
