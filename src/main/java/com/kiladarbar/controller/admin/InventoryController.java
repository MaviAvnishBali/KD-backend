package com.kiladarbar.controller.admin;

import com.kiladarbar.dto.request.CreateInventoryItemRequest;
import com.kiladarbar.dto.request.StockAdjustmentRequest;
import com.kiladarbar.dto.response.ApiResponse;
import com.kiladarbar.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Stock and raw material management")
@PreAuthorize("hasAnyRole('MANAGER', 'OWNER', 'SUPER_ADMIN')")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/items")
    @Operation(summary = "List all inventory items")
    public ResponseEntity<ApiResponse<Page<?>>> listItems(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean lowStock,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.listItems(branchId, category, lowStock, pageable)));
    }

    @PostMapping("/items")
    @Operation(summary = "Create inventory item")
    public ResponseEntity<ApiResponse<?>> createItem(
            @Valid @RequestBody CreateInventoryItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(inventoryService.createItem(request)));
    }

    @PostMapping("/items/{id}/stock-in")
    @Operation(summary = "Receive stock")
    public ResponseEntity<ApiResponse<?>> stockIn(
            @PathVariable UUID id,
            @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.stockIn(id, request)));
    }

    @PostMapping("/items/{id}/stock-out")
    @Operation(summary = "Manual stock deduction")
    public ResponseEntity<ApiResponse<?>> stockOut(
            @PathVariable UUID id,
            @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.stockOut(id, request)));
    }

    @PostMapping("/items/{id}/waste")
    @Operation(summary = "Record waste")
    public ResponseEntity<ApiResponse<?>> recordWaste(
            @PathVariable UUID id,
            @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.recordWaste(id, request)));
    }

    @GetMapping("/low-stock-alerts")
    @Operation(summary = "Get items below reorder level")
    public ResponseEntity<ApiResponse<List<?>>> getLowStockAlerts(
            @RequestParam(required = false) UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getLowStockAlerts(branchId)));
    }

    @GetMapping("/stock-movements")
    @Operation(summary = "Get stock movement history")
    public ResponseEntity<ApiResponse<Page<?>>> getStockMovements(
            @RequestParam(required = false) UUID itemId,
            @RequestParam(required = false) String type,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getStockMovements(itemId, type, pageable)));
    }

    @GetMapping("/consumption-report")
    @Operation(summary = "Get consumption report")
    public ResponseEntity<ApiResponse<?>> getConsumptionReport(
            @RequestParam UUID branchId,
            @RequestParam String period) {  // DAILY, WEEKLY, MONTHLY
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getConsumptionReport(branchId, period)));
    }

    @PostMapping("/purchase-orders")
    @Operation(summary = "Create purchase order")
    public ResponseEntity<ApiResponse<?>> createPurchaseOrder(@RequestBody Object request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(inventoryService.createPurchaseOrder(request)));
    }

    @GetMapping("/purchase-orders")
    public ResponseEntity<ApiResponse<Page<?>>> listPurchaseOrders(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.listPurchaseOrders(pageable)));
    }

    @PatchMapping("/purchase-orders/{id}/receive")
    @Operation(summary = "Mark purchase order as received")
    public ResponseEntity<ApiResponse<?>> receivePurchaseOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.receivePurchaseOrder(id)));
    }
}
