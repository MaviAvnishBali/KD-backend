package com.kiladarbar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PartyHallPackageRequest {
    @NotBlank private String type;
    @NotBlank private String name;
    @NotNull private BigDecimal price;
    private int maxGuests;
    private String emoji;
    private String tagline;
    private List<String> perks;
    private boolean featured;
    private int displayOrder;
}
