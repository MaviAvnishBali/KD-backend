package com.kiladarbar.controller.customer;

import com.kiladarbar.dto.request.AddCartItemRequest;
import com.kiladarbar.dto.request.UpdateCartItemRequest;
import com.kiladarbar.dto.response.ApiResponse;
import com.kiladarbar.dto.response.CartResponse;
import com.kiladarbar.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart operations")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get current cart with calculated totals")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(getUserId(principal))));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                cartService.addItem(getUserId(principal), request), "Item added to cart"));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update item quantity in cart")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                cartService.updateItem(getUserId(principal), itemId, request), "Cart updated"));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID itemId) {
        return ResponseEntity.ok(ApiResponse.success(
                cartService.removeItem(getUserId(principal), itemId), "Item removed"));
    }

    @DeleteMapping
    @Operation(summary = "Clear entire cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal UserDetails principal) {
        cartService.clearCart(getUserId(principal));
        return ResponseEntity.ok(ApiResponse.success("Cart cleared"));
    }

    @PostMapping("/coupon")
    @Operation(summary = "Apply coupon code to cart")
    public ResponseEntity<ApiResponse<CartResponse>> applyCoupon(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam String code) {
        return ResponseEntity.ok(ApiResponse.success(
                cartService.applyCoupon(getUserId(principal), code), "Coupon applied"));
    }

    @DeleteMapping("/coupon")
    @Operation(summary = "Remove applied coupon")
    public ResponseEntity<ApiResponse<CartResponse>> removeCoupon(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                cartService.removeCoupon(getUserId(principal)), "Coupon removed"));
    }

    private UUID getUserId(UserDetails principal) {
        return UUID.fromString(principal.getUsername());
    }
}
