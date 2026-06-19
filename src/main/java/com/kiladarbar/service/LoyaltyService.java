package com.kiladarbar.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface LoyaltyService {
    int getAvailablePoints(UUID userId);
    BigDecimal pointsToRupees(int points);
    void earnPoints(UUID userId, int points, UUID orderId);
    void refundPoints(UUID userId, int points, UUID orderId);
}
