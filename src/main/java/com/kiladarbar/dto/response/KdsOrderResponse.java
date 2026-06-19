package com.kiladarbar.dto.response;

import com.kiladarbar.model.enums.OrderStatus;
import com.kiladarbar.model.enums.OrderType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class KdsOrderResponse {
    private UUID id;
    private String orderNumber;
    private OrderStatus status;
    private OrderType orderType;
    private String tableNumber;
    private LocalDateTime createdAt;
    private int elapsedMinutes;
    private List<KdsItemDto> items;

    @Data @Builder
    public static class KdsItemDto {
        private UUID id;
        private String name;
        private int quantity;
        private String station;
        private String status;
        private String specialInstruction;
    }
}
