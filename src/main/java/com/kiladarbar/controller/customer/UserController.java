package com.kiladarbar.controller.customer;

import com.kiladarbar.dto.request.CreateAddressRequest;
import com.kiladarbar.dto.request.UpdateProfileRequest;
import com.kiladarbar.dto.response.AddressResponse;
import com.kiladarbar.dto.response.ApiResponse;
import com.kiladarbar.dto.response.UserProfileResponse;
import com.kiladarbar.service.DeviceTokenService;
import com.kiladarbar.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User profile & address management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final DeviceTokenService deviceTokenService;

    /* ── Profile ── */

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(uid(auth))));
    }

    @PutMapping("/me")
    @Operation(summary = "Update profile fields (all optional)")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            Authentication auth,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.updateProfile(uid(auth), request), "Profile updated"));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Soft-delete account")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(Authentication auth) {
        userService.deleteAccount(uid(auth));
        return ResponseEntity.ok(ApiResponse.success("Account scheduled for deletion"));
    }

    /* ── Addresses ── */

    @GetMapping("/me/addresses")
    @Operation(summary = "List all saved addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(userService.getAddresses(uid(auth))));
    }

    @PostMapping("/me/addresses")
    @Operation(summary = "Add a new address")
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            Authentication auth,
            @Valid @RequestBody CreateAddressRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.addAddress(uid(auth), request), "Address added"));
    }

    @PutMapping("/me/addresses/{addressId}")
    @Operation(summary = "Update an existing address")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            Authentication auth,
            @PathVariable UUID addressId,
            @Valid @RequestBody CreateAddressRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.updateAddress(uid(auth), addressId, request), "Address updated"));
    }

    @DeleteMapping("/me/addresses/{addressId}")
    @Operation(summary = "Delete an address")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            Authentication auth,
            @PathVariable UUID addressId) {
        userService.deleteAddress(uid(auth), addressId);
        return ResponseEntity.ok(ApiResponse.success("Address removed"));
    }

    @PutMapping("/me/addresses/{addressId}/default")
    @Operation(summary = "Mark an address as default")
    public ResponseEntity<ApiResponse<Void>> setDefaultAddress(
            Authentication auth,
            @PathVariable UUID addressId) {
        userService.setDefaultAddress(uid(auth), addressId);
        return ResponseEntity.ok(ApiResponse.success("Default address updated"));
    }

    /* ── Push notifications ── */

    @PutMapping("/me/fcm-token")
    @Operation(summary = "Register this device's FCM token for push notifications")
    public ResponseEntity<ApiResponse<Void>> updateFcmToken(
            Authentication auth,
            @RequestParam String token) {
        deviceTokenService.register(uid(auth), token, "ANDROID");
        return ResponseEntity.ok(ApiResponse.success("Device registered for notifications"));
    }

    @DeleteMapping("/me/fcm-token")
    @Operation(summary = "Unregister this device (e.g. on logout)")
    public ResponseEntity<ApiResponse<Void>> removeFcmToken(@RequestParam String token) {
        deviceTokenService.remove(token);
        return ResponseEntity.ok(ApiResponse.success("Device unregistered"));
    }

    /* ── Helper ── */

    private UUID uid(Authentication auth) {
        return UUID.fromString(auth.getName());
    }
}
