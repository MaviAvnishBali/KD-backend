package com.kiladarbar.dto.request;

import com.kiladarbar.model.enums.FoodType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateMenuItemRequest {
    @Size(max = 150) private String name;
    private String description;
    @DecimalMin("0.01") private BigDecimal price;
    @DecimalMin("0.01") private BigDecimal discountPrice;
    private FoodType foodType;
    private int preparationTime;
    private BigDecimal gstRate;
    private Boolean isAvailable;
}
