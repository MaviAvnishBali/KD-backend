package com.kiladarbar.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data @Builder @JsonInclude(JsonInclude.Include.NON_NULL)
public class BannerResponse {
    private UUID   id;
    private String title;
    private String subtitle;
    private String tag;
    private String emoji;
    private String bgColorStart;
    private String bgColorEnd;
    private String ctaText;
    private String ctaLink;
    private int    displayOrder;
}
