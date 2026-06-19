package com.kiladarbar.controller.pos;

import com.kiladarbar.dto.request.PosOrderRequest;
import com.kiladarbar.dto.response.ApiResponse;
import com.kiladarbar.dto.response.PosReceiptResponse;
import com.kiladarbar.service.PosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/pos")
@RequiredArgsConstructor
@Tag(name = "POS", description = "Point of Sale billing operations")
@PreAuthorize("hasAnyRole('CASHIER', 'MANAGER', 'OWNER')")
public class PosController {

    private final PosService posService;

    @PostMapping("/orders")
    @Operation(summary = "Create POS order and generate bill")
    public ResponseEntity<ApiResponse<PosReceiptResponse>> createPosOrder(
            @Valid @RequestBody PosOrderRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                posService.createPosOrder(request, UUID.fromString(principal.getUsername()))));
    }

    @GetMapping("/orders/{id}/receipt")
    @Operation(summary = "Get receipt for a POS order")
    public ResponseEntity<ApiResponse<PosReceiptResponse>> getReceipt(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(posService.getReceipt(id)));
    }

    @PostMapping("/orders/{id}/payment")
    @Operation(summary = "Record payment for a POS order")
    public ResponseEntity<ApiResponse<PosReceiptResponse>> recordPayment(
            @PathVariable UUID id,
            @RequestParam String method,
            @RequestParam(required = false) String reference) {
        return ResponseEntity.ok(ApiResponse.success(
                posService.recordPayment(id, method, reference)));
    }

    @PostMapping("/orders/{id}/split")
    @Operation(summary = "Split bill between customers")
    public ResponseEntity<ApiResponse<?>> splitBill(
            @PathVariable UUID id,
            @RequestParam int splitBy) {
        return ResponseEntity.ok(ApiResponse.success(posService.splitBill(id, splitBy)));
    }

    @PostMapping("/orders/{id}/void")
    @Operation(summary = "Void a POS order (requires manager PIN)")
    public ResponseEntity<ApiResponse<Void>> voidOrder(
            @PathVariable UUID id,
            @RequestParam String managerPin,
            @RequestParam String reason) {
        posService.voidOrder(id, managerPin, reason);
        return ResponseEntity.ok(ApiResponse.success("Order voided successfully"));
    }

    @GetMapping("/tables")
    @Operation(summary = "Get table status for POS floor view")
    public ResponseEntity<ApiResponse<?>> getTableStatus(
            @RequestParam(required = false) UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(posService.getTableStatus(branchId)));
    }

    @GetMapping("/summary/day-end")
    @Operation(summary = "Get day-end settlement summary")
    public ResponseEntity<ApiResponse<?>> getDayEndSummary(
            @RequestParam UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(posService.getDayEndSummary(branchId)));
    }

    @PostMapping("/upi-qr/{orderId}")
    @Operation(summary = "Generate UPI QR for POS billing")
    public ResponseEntity<ApiResponse<String>> generateUpiQr(@PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.success(posService.generateUpiQr(orderId)));
    }
}
