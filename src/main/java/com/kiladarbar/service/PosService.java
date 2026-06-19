package com.kiladarbar.service;

import com.kiladarbar.dto.request.PosOrderRequest;
import com.kiladarbar.dto.response.PosReceiptResponse;
import java.util.UUID;

public interface PosService {
    PosReceiptResponse createPosOrder(PosOrderRequest request, UUID cashierId);
    PosReceiptResponse getReceipt(UUID orderId);
    PosReceiptResponse recordPayment(UUID orderId, String method, String reference);
    Object splitBill(UUID orderId, int splitBy);
    void voidOrder(UUID orderId, String managerPin, String reason);
    Object getTableStatus(UUID branchId);
    Object getDayEndSummary(UUID branchId);
    String generateUpiQr(UUID orderId);
}
