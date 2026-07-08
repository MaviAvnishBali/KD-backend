package com.kiladarbar.controller.customer;

import com.kiladarbar.dto.request.CreatePartyHallBookingRequest;
import com.kiladarbar.dto.response.ApiResponse;
import com.kiladarbar.dto.response.PartyHallBookingResponse;
import com.kiladarbar.service.PartyHallBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/party-hall/bookings")
@RequiredArgsConstructor
@Tag(name = "Party Hall", description = "Party hall / banquet booking enquiries")
public class PartyHallController {

    private final PartyHallBookingService bookingService;

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a party hall booking enquiry")
    public ResponseEntity<ApiResponse<PartyHallBookingResponse>> create(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CreatePartyHallBookingRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                bookingService.create(getUserId(principal), request), "Booking request submitted"));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current user's party hall bookings")
    public ResponseEntity<ApiResponse<List<PartyHallBookingResponse>>> getMyBookings(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getUserBookings(getUserId(principal))));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cancel a party hall booking")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id) {
        bookingService.cancel(getUserId(principal), id);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled"));
    }

    private UUID getUserId(UserDetails principal) {
        return UUID.fromString(principal.getUsername());
    }
}
