package com.kiladarbar.controller.customer;

import com.kiladarbar.dto.request.CreateReservationRequest;
import com.kiladarbar.dto.response.ApiResponse;
import com.kiladarbar.dto.response.ReservationResponse;
import com.kiladarbar.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Table booking management")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a table reservation")
    public ResponseEntity<ApiResponse<ReservationResponse>> create(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CreateReservationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                reservationService.create(getUserId(principal), request), "Reservation confirmed"));
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current user's reservations")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getUserReservations(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(reservationService.getUserReservations(getUserId(principal))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation details")
    public ResponseEntity<ApiResponse<ReservationResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(reservationService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cancel a reservation")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id) {
        reservationService.cancel(getUserId(principal), id);
        return ResponseEntity.ok(ApiResponse.success("Reservation cancelled"));
    }

    @GetMapping("/availability")
    @Operation(summary = "Check table availability for a date & party size")
    public ResponseEntity<ApiResponse<List<String>>> checkAvailability(
            @RequestParam UUID branchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam int partySize) {
        return ResponseEntity.ok(ApiResponse.success(
                reservationService.getAvailableSlots(branchId, date, partySize)));
    }

    private UUID getUserId(UserDetails principal) {
        return UUID.fromString(principal.getUsername());
    }
}
