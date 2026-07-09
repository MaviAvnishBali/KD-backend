package com.kiladarbar.controller.admin;

import com.kiladarbar.dto.response.ApiResponse;
import com.kiladarbar.dto.response.ReservationResponse;
import com.kiladarbar.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER', 'OWNER', 'SUPER_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin · Reservations", description = "Manage table reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    @GetMapping
    @Operation(summary = "List all table reservations")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> list(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(reservationService.adminList(status)));
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "Confirm a reservation")
    public ResponseEntity<ApiResponse<ReservationResponse>> confirm(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(reservationService.adminConfirm(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel / delete a reservation")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        reservationService.adminDelete(id);
        return ResponseEntity.ok(ApiResponse.success("Reservation cancelled"));
    }
}
