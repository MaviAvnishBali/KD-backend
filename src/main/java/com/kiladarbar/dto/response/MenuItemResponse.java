package com.kiladarbar.dto.response;

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
    private boolean isAvailable;
    private boolean isBestSeller;
    private boolean isRecommended;
    private boolean isSeasonal;
    private int preparationTime;
    private Integer calories;
    private BigDecimal gstRate;
    private List<String> imageUrls;
    private Integer categoryId;
    private String categoryName;
}
