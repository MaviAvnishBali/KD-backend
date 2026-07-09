package com.kiladarbar.controller.admin;

import com.kiladarbar.dto.response.ApiResponse;
import com.kiladarbar.dto.response.PartyHallBookingResponse;
import com.kiladarbar.service.PartyHallBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/party-hall/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER', 'OWNER', 'SUPER_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin · Party Hall", description = "Manage party hall / banquet bookings")
public class AdminPartyHallController {

    private final PartyHallBookingService bookingService;

    @GetMapping
    @Operation(summary = "List all party hall bookings")
    public ResponseEntity<ApiResponse<List<PartyHallBookingResponse>>> list(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.adminList(status)));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update a booking's status")
    public ResponseEntity<ApiResponse<PartyHallBookingResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(
                bookingService.adminUpdateStatus(id, body.get("status"))));
    }

    @PutMapping("/{id}/package")
    @Operation(summary = "Set a booking's package & confirmed price")
    public ResponseEntity<ApiResponse<PartyHallBookingResponse>> updatePackage(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {
        String packageType = body.get("packageType") != null ? body.get("packageType").toString() : null;
        Object amount = body.get("totalAmount");
        BigDecimal totalAmount = amount != null ? new BigDecimal(amount.toString()) : null;
        return ResponseEntity.ok(ApiResponse.success(
                bookingService.adminUpdatePackage(id, packageType, totalAmount)));
    }
}
