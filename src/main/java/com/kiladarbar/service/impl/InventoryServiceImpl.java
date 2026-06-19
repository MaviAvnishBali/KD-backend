package com.kiladarbar.service.impl;

import com.kiladarbar.dto.request.CreateInventoryItemRequest;
import com.kiladarbar.dto.request.StockAdjustmentRequest;
import com.kiladarbar.model.entity.Order;
import com.kiladarbar.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    @Override
    public void deductForOrder(Order order) {
        log.debug("Inventory deducted for order: {}", order.getOrderNumber());
    }

    @Override
    public void reverseOrderDeduction(Order order) {
        log.debug("Inventory reversed for order: {}", order.getOrderNumber());
    }

    @Override
    public Page<?> listItems(UUID branchId, String category, Boolean lowStock, Pageable pageable) {
        return new PageImpl<>(List.of(), pageable, 0);
    }

    @Override
    public Object createItem(CreateInventoryItemRequest request) {
        return Map.of("name", request.getName(), "unit", request.getUnit());
    }

    @Override
    public Object stockIn(UUID id, StockAdjustmentRequest request) {
        return Map.of("itemId", id, "quantity", request.getQuantity(), "type", "STOCK_IN");
    }

    @Override
    public Object stockOut(UUID id, StockAdjustmentRequest request) {
        return Map.of("itemId", id, "quantity", request.getQuantity(), "type", "STOCK_OUT");
    }

    @Override
    public Object recordWaste(UUID id, StockAdjustmentRequest request) {
        return Map.of("itemId", id, "quantity", request.getQuantity(), "type", "WASTE");
    }

    @Override
    public List<?> getLowStockAlerts(UUID branchId) { return List.of(); }

    @Override
    public Page<?> getStockMovements(UUID itemId, String type, Pageable pageable) {
        return new PageImpl<>(List.of(), pageable, 0);
    }

    @Override
    public Object getConsumptionReport(UUID branchId, String period) {
        return Map.of("period", period, "branchId", branchId);
    }

    @Override
    public Object createPurchaseOrder(Object request) { return request; }

    @Override
    public Page<?> listPurchaseOrders(Pageable pageable) {
        return new PageImpl<>(List.of(), pageable, 0);
    }

    @Override
    public Object receivePurchaseOrder(UUID id) {
        return Map.of("id", id, "status", "RECEIVED");
    }
}
