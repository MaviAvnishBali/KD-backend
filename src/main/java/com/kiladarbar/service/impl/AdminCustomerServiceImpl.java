package com.kiladarbar.service.impl;

import com.kiladarbar.dto.projection.CustomerOrderStats;
import com.kiladarbar.dto.response.CustomerResponse;
import com.kiladarbar.model.entity.LoyaltyAccount;
import com.kiladarbar.model.entity.User;
import com.kiladarbar.repository.OrderRepository;
import com.kiladarbar.repository.UserRepository;
import com.kiladarbar.service.AdminCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCustomerServiceImpl implements AdminCustomerService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    public Page<CustomerResponse> listCustomers(String tier, Pageable pageable) {
        String tierFilter = (tier == null || tier.isBlank()) ? null : tier;
        Page<User> users = userRepository.findCustomers(tierFilter, pageable);

        List<UUID> ids = users.getContent().stream().map(User::getId).toList();
        Map<UUID, CustomerOrderStats> statsById = ids.isEmpty()
                ? Map.of()
                : orderRepository.aggregateForUsers(ids).stream()
                        .collect(Collectors.toMap(CustomerOrderStats::getUserId, Function.identity()));

        return users.map(u -> toResponse(u, statsById.get(u.getId())));
    }

    private CustomerResponse toResponse(User u, CustomerOrderStats stats) {
        LoyaltyAccount loyalty = u.getLoyaltyAccount();
        return CustomerResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .phone(u.getPhone())
                .email(u.getEmail())
                .totalOrders(stats != null ? stats.getOrderCount() : 0)
                .totalSpend(stats != null && stats.getTotalSpend() != null ? stats.getTotalSpend() : BigDecimal.ZERO)
                .loyaltyPoints(loyalty != null ? loyalty.getPoints() : 0)
                .tier(loyalty != null ? loyalty.getTier() : "BRONZE")
                .createdAt(u.getCreatedAt())
                .lastOrderAt(stats != null ? stats.getLastOrderAt() : null)
                .build();
    }
}
