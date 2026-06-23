package com.kiladarbar.service;

import com.kiladarbar.dto.request.AddCartItemRequest;
import com.kiladarbar.dto.request.UpdateCartItemRequest;
import com.kiladarbar.dto.response.CartResponse;

import java.util.UUID;

public interface CartService {
    CartResponse getCart(UUID userId);
    CartResponse addItem(UUID userId, AddCartItemRequest request);
    CartResponse updateItem(UUID userId, UUID itemId, UpdateCartItemRequest request);
    CartResponse removeItem(UUID userId, UUID itemId);
    void clearCart(UUID userId);
    CartResponse applyCoupon(UUID userId, String couponCode);
    CartResponse removeCoupon(UUID userId);
}
