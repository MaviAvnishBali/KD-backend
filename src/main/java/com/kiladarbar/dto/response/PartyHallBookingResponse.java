package com.kiladarbar.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartyHallBookingResponse {
    private UUID id;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String eventType;
    private int guestCount;
    private String preferredDate;
    private String preferredTime;
    private String packageType;
    private String specialRequests;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
