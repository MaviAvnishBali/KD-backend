package com.kiladarbar.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateInventoryItemRequest {
    @NotNull private UUID branchId;
    @NotBlank @Size(max = 100) private String name;
    @NotBlank private String unit;
    private String category;
    @DecimalMin("0") private BigDecimal currentStock;
    @DecimalMin("0") private BigDecimal reorderLevel;
    @DecimalMin("0") private BigDecimal unitCost;
}
