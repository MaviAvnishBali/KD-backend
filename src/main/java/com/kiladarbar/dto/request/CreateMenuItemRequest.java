package com.kiladarbar.dto.request;

import com.kiladarbar.model.enums.FoodType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateMenuItemRequest {
    private UUID branchId;   // optional — defaults to the item having no branch (single-branch setup)
    @NotNull private Integer categoryId;
    @NotBlank @Size(max = 150) private String name;
    private String description;
    @NotNull @DecimalMin("0.01") private BigDecimal price;
    @DecimalMin("0.01") private BigDecimal discountPrice;
    @NotNull private FoodType foodType;
    private int preparationTime;
    private BigDecimal gstRate;
}
