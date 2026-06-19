package com.kiladarbar.service;

import com.kiladarbar.dto.response.KdsOrderResponse;
import java.util.List;
import java.util.UUID;

public interface KdsService {
    List<KdsOrderResponse> getActiveOrders(UUID branchId, String station);
    KdsOrderResponse startPreparing(UUID orderId);
    KdsOrderResponse markReady(UUID orderId);
    void markItemReady(UUID orderId, UUID itemId);
    Object getKitchenStats(UUID branchId);
}
