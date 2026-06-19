package com.kiladarbar.controller.kds;

import com.kiladarbar.dto.response.ApiResponse;
import com.kiladarbar.dto.response.KdsOrderResponse;
import com.kiladarbar.service.KdsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/kds")
@RequiredArgsConstructor
@Tag(name = "KDS", description = "Kitchen Display System endpoints")
@PreAuthorize("hasAnyRole('CHEF', 'KITCHEN_STAFF', 'MANAGER', 'OWNER')")
public class KdsController {

    private final KdsService kdsService;

    @GetMapping("/orders")
    @Operation(summary = "Get all active kitchen orders")
    public ResponseEntity<ApiResponse<List<KdsOrderResponse>>> getActiveOrders(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) String station) {
        return ResponseEntity.ok(ApiResponse.success(
                kdsService.getActiveOrders(branchId, station)));
    }

    @PatchMapping("/orders/{orderId}/start")
    @Operation(summary = "Mark order as being prepared")
    public ResponseEntity<ApiResponse<KdsOrderResponse>> startPreparing(@PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.success(kdsService.startPreparing(orderId)));
    }

    @PatchMapping("/orders/{orderId}/ready")
    @Operation(summary = "Mark order as ready")
    public ResponseEntity<ApiResponse<KdsOrderResponse>> markReady(@PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.success(kdsService.markReady(orderId)));
    }

    @PatchMapping("/orders/{orderId}/items/{itemId}/ready")
    @Operation(summary = "Mark individual item as ready")
    public ResponseEntity<ApiResponse<Void>> markItemReady(
            @PathVariable UUID orderId,
            @PathVariable UUID itemId) {
        kdsService.markItemReady(orderId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item marked ready"));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get kitchen performance stats")
    public ResponseEntity<ApiResponse<?>> getKitchenStats(
            @RequestParam(required = false) UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(kdsService.getKitchenStats(branchId)));
    }

    @MessageMapping("/kds.update")
    @SendTo("/topic/kds")
    public KdsOrderResponse handleKdsUpdate(KdsOrderResponse order) {
        return order;
    }
}
