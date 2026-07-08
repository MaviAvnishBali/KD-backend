package com.kiladarbar.controller.delivery;

import com.kiladarbar.dto.request.CompleteDeliveryRequest;
import com.kiladarbar.dto.request.DriverAvailabilityRequest;
import com.kiladarbar.dto.request.DriverLocationRequest;
import com.kiladarbar.dto.response.ApiResponse;
import com.kiladarbar.dto.response.DeliveryEarningsResponse;
import com.kiladarbar.dto.response.DeliveryOrderResponse;
import com.kiladarbar.dto.response.DeliveryPartnerProfileResponse;
import com.kiladarbar.service.DeliveryService;
import com.kiladarbar.service.DeviceTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/delivery")
@RequiredArgsConstructor
@Tag(name = "Delivery Partner", description = "Driver app APIs")
@SecurityRequirement(name = "bearerAuth")
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final DeviceTokenService deviceTokenService;

    // ── Push token ──

    @PutMapping("/fcm-token")
    @Operation(summary = "Register this device's FCM token for delivery push")
    public ResponseEntity<ApiResponse<Void>> registerFcmToken(
            @RequestParam String token,
            @AuthenticationPrincipal UserDetails principal) {
        deviceTokenService.register(userId(principal), token, "ANDROID");
        return ResponseEntity.ok(ApiResponse.success("Token registered"));
    }

    @DeleteMapping("/fcm-token")
    @Operation(summary = "Remove this device's FCM token")
    public ResponseEntity<ApiResponse<Void>> removeFcmToken(@RequestParam String token) {
        deviceTokenService.remove(token);
        return ResponseEntity.ok(ApiResponse.success("Token removed"));
    }

    // ── Profile & presence ──

    @GetMapping("/me")
    @Operation(summary = "Get my delivery partner profile")
    public ResponseEntity<ApiResponse<DeliveryPartnerProfileResponse>> me(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(deliveryService.getProfile(userId(principal))));
    }

    @PutMapping("/availability")
    @Operation(summary = "Go online / offline")
    public ResponseEntity<ApiResponse<Void>> setAvailability(
            @Valid @RequestBody DriverAvailabilityRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        deliveryService.setAvailability(userId(principal), request.getAvailable());
        return ResponseEntity.ok(ApiResponse.success(
                request.getAvailable() ? "You are online" : "You are offline"));
    }

    @PutMapping("/location")
    @Operation(summary = "Update current location")
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @Valid @RequestBody DriverLocationRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        deliveryService.updateLocation(userId(principal), request.getLat(), request.getLng());
        return ResponseEntity.ok(ApiResponse.success("Location updated"));
    }

    // ── Orders ──

    @GetMapping("/orders/active")
    @Operation(summary = "My active deliveries (assigned / accepted / picked up)")
    public ResponseEntity<ApiResponse<List<DeliveryOrderResponse>>> activeOrders(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(deliveryService.getActiveOrders(userId(principal))));
    }

    @GetMapping("/orders/history")
    @Operation(summary = "My completed deliveries")
    public ResponseEntity<ApiResponse<Page<DeliveryOrderResponse>>> history(
            @AuthenticationPrincipal UserDetails principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                deliveryService.getDeliveryHistory(userId(principal), pageable)));
    }

    @GetMapping("/orders/{assignmentId}")
    @Operation(summary = "Get one delivery assignment")
    public ResponseEntity<ApiResponse<DeliveryOrderResponse>> getOne(
            @PathVariable UUID assignmentId,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                deliveryService.getAssignment(assignmentId, userId(principal))));
    }

    @PostMapping("/orders/{assignmentId}/accept")
    @Operation(summary = "Accept an assigned delivery")
    public ResponseEntity<ApiResponse<DeliveryOrderResponse>> accept(
            @PathVariable UUID assignmentId,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                deliveryService.acceptAssignment(assignmentId, userId(principal)),
                "Delivery accepted"));
    }

    @PostMapping("/orders/{assignmentId}/reject")
    @Operation(summary = "Reject an assigned delivery")
    public ResponseEntity<ApiResponse<DeliveryOrderResponse>> reject(
            @PathVariable UUID assignmentId,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                deliveryService.rejectAssignment(assignmentId, userId(principal)),
                "Delivery rejected"));
    }

    @PostMapping("/orders/{assignmentId}/pickup")
    @Operation(summary = "Mark order picked up from restaurant (goes out for delivery)")
    public ResponseEntity<ApiResponse<DeliveryOrderResponse>> pickup(
            @PathVariable UUID assignmentId,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                deliveryService.markPickedUp(assignmentId, userId(principal)),
                "Order picked up"));
    }

    @PostMapping("/orders/{assignmentId}/deliver")
    @Operation(summary = "Complete delivery (confirm COD cash collected)")
    public ResponseEntity<ApiResponse<DeliveryOrderResponse>> deliver(
            @PathVariable UUID assignmentId,
            @Valid @RequestBody CompleteDeliveryRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                deliveryService.completeDelivery(assignmentId, userId(principal), request),
                "Delivery completed"));
    }

    // ── Earnings ──

    @GetMapping("/earnings")
    @Operation(summary = "My earnings summary")
    public ResponseEntity<ApiResponse<DeliveryEarningsResponse>> earnings(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(deliveryService.getEarnings(userId(principal))));
    }

    private UUID userId(UserDetails principal) {
        return UUID.fromString(principal.getUsername());
    }
}
