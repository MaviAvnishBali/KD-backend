package com.kiladarbar.dto.request;

import com.kiladarbar.model.enums.OrderType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class PosOrderRequest {
    @NotNull private UUID branchId;
    private UUID tableId;
    @NotNull private OrderType orderType;
    @NotNull private List<PosOrderItemDto> items;
    private String customerPhone;
    private String paymentMethod;

    @Data
    public static class PosOrderItemDto {
        private UUID menuItemId;
        private int quantity;
        private String specialInstruction;
    }
}
