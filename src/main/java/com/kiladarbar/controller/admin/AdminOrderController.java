package com.kiladarbar.controller.admin;

import com.kiladarbar.dto.response.ApiResponse;
import com.kiladarbar.dto.response.OrderResponse;
import com.kiladarbar.model.enums.OrderStatus;
import com.kiladarbar.model.enums.OrderType;
import com.kiladarbar.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@Tag(name = "Admin - Orders", description = "Order management for admins")
@PreAuthorize("hasAnyRole('MANAGER', 'OWNER', 'SUPER_ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "List all orders with filters")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> listOrders(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) OrderType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.adminListOrders(branchId, status, type, from, to, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.adminGetOrder(id)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success(orderService.updateOrderStatus(id, status)));
    }

    @PatchMapping("/{id}/assign-driver")
    @Operation(summary = "Assign delivery driver")
    public ResponseEntity<ApiResponse<OrderResponse>> assignDriver(
            @PathVariable UUID id,
            @RequestParam UUID driverId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.assignDriver(id, driverId)));
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Initiate order refund")
    public ResponseEntity<ApiResponse<Void>> refundOrder(
            @PathVariable UUID id,
            @RequestParam String reason) {
        orderService.refundOrder(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Refund initiated successfully"));
    }
}
