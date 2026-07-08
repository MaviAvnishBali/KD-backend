package com.kiladarbar.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kiladarbar.model.enums.FoodType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class MenuItemResponse {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private BigDecimal effectivePrice;
    private FoodType foodType;
    // Serialize with the "is" prefix that all clients (admin/customer web, mobile) expect.
    // Without this Lombok's getter makes Jackson emit "available"/"bestSeller"/etc.
    @JsonProperty("isAvailable")   private boolean isAvailable;
    @JsonProperty("isBestSeller")  private boolean isBestSeller;
    @JsonProperty("isRecommended") private boolean isRecommended;
    @JsonProperty("isSeasonal")    private boolean isSeasonal;
    private int preparationTime;
    private Integer calories;
    private BigDecimal gstRate;
    private List<String> imageUrls;
    private Integer categoryId;
    private String categoryName;
}
