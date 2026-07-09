package com.kiladarbar.dto.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Aggregated order stats for a single customer (per-user GROUP BY projection). */
public interface CustomerOrderStats {
    UUID getUserId();
    long getOrderCount();
    BigDecimal getTotalSpend();
    LocalDateTime getLastOrderAt();
}
