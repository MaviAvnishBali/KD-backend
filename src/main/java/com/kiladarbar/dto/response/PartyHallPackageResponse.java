package com.kiladarbar.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartyHallPackageResponse {
    private UUID id;
    private String type;
    private String name;
    private BigDecimal price;
    private int maxGuests;
    private String emoji;
    private String tagline;
    private List<String> perks;
    private boolean featured;
    private boolean active;
    private int displayOrder;
}
