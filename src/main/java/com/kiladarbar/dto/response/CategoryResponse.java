package com.kiladarbar.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class CategoryResponse {
    private Integer id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private int displayOrder;
    private List<CategoryResponse> subCategories;
}
