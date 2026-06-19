package com.kiladarbar.controller.customer;

import com.kiladarbar.dto.request.CreateOrderRequest;
import com.kiladarbar.dto.request.RateOrderRequest;
import com.kiladarbar.dto.response.ApiResponse;
import com.kiladarbar.dto.response.OrderResponse;
import com.kiladarbar.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order placement and tracking")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place a new order")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        OrderResponse order = orderService.placeOrder(request, getUserId(principal));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(order, "Order placed successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getOrderById(id, getUserId(principal))));
    }

    @GetMapping("/me")
    @Operation(summary = "Get my order history")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal UserDetails principal,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getUserOrders(getUserId(principal), pageable)));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.cancelOrder(id, reason, getUserId(principal))));
    }

    @GetMapping("/{id}/track")
    @Operation(summary = "Get live order tracking")
    public ResponseEntity<ApiResponse<OrderResponse>> trackOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.trackOrder(id, getUserId(principal))));
    }

    @PostMapping("/{id}/rate")
    @Operation(summary = "Rate an order")
    public ResponseEntity<ApiResponse<Void>> rateOrder(
            @PathVariable UUID id,
            @Valid @RequestBody RateOrderRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        orderService.rateOrder(id, request, getUserId(principal));
        return ResponseEntity.ok(ApiResponse.success("Review submitted successfully"));
    }

    @PostMapping("/{id}/reorder")
    @Operation(summary = "Reorder the same items")
    public ResponseEntity<ApiResponse<OrderResponse>> reorder(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(orderService.reorder(id, getUserId(principal))));
    }

    private UUID getUserId(UserDetails principal) {
        return UUID.fromString(principal.getUsername());
    }
}
