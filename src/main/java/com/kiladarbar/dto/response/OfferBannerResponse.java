package com.kiladarbar.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OfferBannerResponse {
    private UUID          id;
    private String        emoji;
    private String        title;
    private String        description;
    private String        promoCode;
    private String        savingText;
    private String        badgeText;
    private String        bgColorStart;
    private String        bgColorEnd;
    private String        imageUrl;
    private String        discountType;
    private BigDecimal    discountValue;
    private BigDecimal    minOrderAmount;
    private BigDecimal    maxDiscount;
    private OffsetDateTime validUntil;
    private int           displayOrder;
}
