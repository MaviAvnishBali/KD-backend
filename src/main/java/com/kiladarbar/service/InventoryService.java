package com.kiladarbar.service;

import com.kiladarbar.dto.request.CreateInventoryItemRequest;
import com.kiladarbar.dto.request.StockAdjustmentRequest;
import com.kiladarbar.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface InventoryService {
    void deductForOrder(Order order);
    void reverseOrderDeduction(Order order);
    Page<?> listItems(UUID branchId, String category, Boolean lowStock, Pageable pageable);
    Object createItem(CreateInventoryItemRequest request);
    Object stockIn(UUID id, StockAdjustmentRequest request);
    Object stockOut(UUID id, StockAdjustmentRequest request);
    Object recordWaste(UUID id, StockAdjustmentRequest request);
    List<?> getLowStockAlerts(UUID branchId);
    Page<?> getStockMovements(UUID itemId, String type, Pageable pageable);
    Object getConsumptionReport(UUID branchId, String period);
    Object createPurchaseOrder(Object request);
    Page<?> listPurchaseOrders(Pageable pageable);
    Object receivePurchaseOrder(UUID id);
}
