package com.kiladarbar.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReservationResponse {
    private UUID id;
    private String branchName;
    private String customerName;
    private String customerPhone;
    private int partySize;
    private LocalDate reservedDate;
    private LocalTime reservedTime;
    private String occasion;
    private String specialRequest;
    private String status;
    private String tableNumber;
}
