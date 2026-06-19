package com.kiladarbar.controller.admin;

import com.kiladarbar.dto.response.ApiResponse;
import com.kiladarbar.service.ReportsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Analytics and reporting endpoints")
@PreAuthorize("hasAnyRole('MANAGER', 'OWNER', 'SUPER_ADMIN')")
public class ReportsController {

    private final ReportsService reportsService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard overview stats")
    public ResponseEntity<ApiResponse<?>> getDashboard(
            @RequestParam(required = false) UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(reportsService.getDashboardStats(branchId)));
    }

    @GetMapping("/sales")
    @Operation(summary = "Get sales report by period")
    public ResponseEntity<ApiResponse<?>> getSalesReport(
            @RequestParam(required = false) UUID branchId,
            @RequestParam String period,  // DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(
                reportsService.getSalesReport(branchId, period, from, to)));
    }

    @GetMapping("/items/top-selling")
    @Operation(summary = "Get top selling items")
    public ResponseEntity<ApiResponse<?>> getTopSellingItems(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(
                reportsService.getTopSellingItems(branchId, limit, from, to)));
    }

    @GetMapping("/customers")
    @Operation(summary = "Get customer analytics report")
    public ResponseEntity<ApiResponse<?>> getCustomerReport(
            @RequestParam(required = false) UUID branchId,
            @RequestParam String period) {
        return ResponseEntity.ok(ApiResponse.success(
                reportsService.getCustomerReport(branchId, period)));
    }

    @GetMapping("/delivery")
    @Operation(summary = "Get delivery performance report")
    public ResponseEntity<ApiResponse<?>> getDeliveryReport(
            @RequestParam(required = false) UUID branchId,
            @RequestParam String period) {
        return ResponseEntity.ok(ApiResponse.success(
                reportsService.getDeliveryReport(branchId, period)));
    }

    @GetMapping("/staff")
    @Operation(summary = "Get staff performance report")
    public ResponseEntity<ApiResponse<?>> getStaffReport(
            @RequestParam(required = false) UUID branchId,
            @RequestParam String period) {
        return ResponseEntity.ok(ApiResponse.success(
                reportsService.getStaffReport(branchId, period)));
    }

    @GetMapping("/profit")
    @Operation(summary = "Get profit and loss analysis")
    public ResponseEntity<ApiResponse<?>> getProfitAnalysis(
            @RequestParam(required = false) UUID branchId,
            @RequestParam String period) {
        return ResponseEntity.ok(ApiResponse.success(
                reportsService.getProfitAnalysis(branchId, period)));
    }

    @GetMapping("/export/sales")
    @Operation(summary = "Export sales report as PDF/Excel/CSV")
    public ResponseEntity<byte[]> exportSalesReport(
            @RequestParam(required = false) UUID branchId,
            @RequestParam String period,
            @RequestParam String format,  // PDF, EXCEL, CSV
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        byte[] data = reportsService.exportSalesReport(branchId, period, format, from, to);
        MediaType mediaType = resolveMediaType(format);
        String filename = "sales-report." + format.toLowerCase();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(data);
    }

    private MediaType resolveMediaType(String format) {
        return switch (format.toUpperCase()) {
            case "PDF" -> MediaType.APPLICATION_PDF;
            case "EXCEL" -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            default -> MediaType.parseMediaType("text/csv");
        };
    }
}
