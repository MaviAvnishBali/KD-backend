package com.kiladarbar.service.impl;

import com.kiladarbar.model.entity.LoyaltyAccount;
import com.kiladarbar.repository.LoyaltyAccountRepository;
import com.kiladarbar.service.LoyaltyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LoyaltyServiceImpl implements LoyaltyService {

    private static final BigDecimal RUPEES_PER_POINT = new BigDecimal("0.10");
    private final LoyaltyAccountRepository loyaltyAccountRepository;

    @Override
    @Transactional(readOnly = true)
    public int getAvailablePoints(UUID userId) {
        return loyaltyAccountRepository.findByUserId(userId)
                .map(LoyaltyAccount::getPoints).orElse(0);
    }

    @Override
    public BigDecimal pointsToRupees(int points) {
        return BigDecimal.valueOf(points).multiply(RUPEES_PER_POINT);
    }

    @Override
    public void earnPoints(UUID userId, int points, UUID orderId) {
        loyaltyAccountRepository.findByUserId(userId).ifPresent(account -> {
            account.setPoints(account.getPoints() + points);
            account.setLifetimePoints(account.getLifetimePoints() + points);
            loyaltyAccountRepository.save(account);
        });
    }

    @Override
    public void refundPoints(UUID userId, int points, UUID orderId) {
        loyaltyAccountRepository.findByUserId(userId).ifPresent(account -> {
            account.setPoints(Math.max(0, account.getPoints() + points));
            loyaltyAccountRepository.save(account);
        });
    }
}
