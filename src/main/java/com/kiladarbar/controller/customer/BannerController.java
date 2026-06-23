package com.kiladarbar.controller.customer;

import com.kiladarbar.dto.response.ApiResponse;
import com.kiladarbar.dto.response.BannerResponse;
import com.kiladarbar.dto.response.OfferBannerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "Banners", description = "Hero banners and offer cards for the home screen")
public class BannerController {

    private final JdbcTemplate jdbc;

    @GetMapping("/banners")
    @Operation(summary = "Get active hero banner slides")
    public ResponseEntity<ApiResponse<List<BannerResponse>>> getBanners() {
        List<BannerResponse> banners = jdbc.query(
            "SELECT id, title, subtitle, tag, emoji, bg_color_start, bg_color_end, cta_text, cta_link, display_order " +
            "FROM banners WHERE is_active = true ORDER BY display_order",
            (rs, i) -> BannerResponse.builder()
                .id(UUID.fromString(rs.getString("id")))
                .title(rs.getString("title"))
                .subtitle(rs.getString("subtitle"))
                .tag(rs.getString("tag"))
                .emoji(rs.getString("emoji"))
                .bgColorStart(rs.getString("bg_color_start"))
                .bgColorEnd(rs.getString("bg_color_end"))
                .ctaText(rs.getString("cta_text"))
                .ctaLink(rs.getString("cta_link"))
                .displayOrder(rs.getInt("display_order"))
                .build()
        );
        return ResponseEntity.ok(ApiResponse.success(banners));
    }

    @GetMapping("/offers")
    @Operation(summary = "Get active offer banners — includes image URL and full pricing details")
    public ResponseEntity<ApiResponse<List<OfferBannerResponse>>> getOffers() {
        List<OfferBannerResponse> offers = jdbc.query(
            "SELECT id, emoji, title, description, promo_code, saving_text, badge_text, " +
            "       bg_color_start, bg_color_end, image_url, " +
            "       discount_type, discount_value, min_order_amount, max_discount, " +
            "       valid_until, display_order " +
            "FROM offer_banners " +
            "WHERE is_active = true " +
            "  AND (valid_until IS NULL OR valid_until > NOW()) " +
            "ORDER BY display_order",
            (rs, i) -> {
                String discValStr = rs.getString("discount_value");
                String minOrdStr  = rs.getString("min_order_amount");
                String maxDiscStr = rs.getString("max_discount");
                return OfferBannerResponse.builder()
                    .id(UUID.fromString(rs.getString("id")))
                    .emoji(rs.getString("emoji"))
                    .title(rs.getString("title"))
                    .description(rs.getString("description"))
                    .promoCode(rs.getString("promo_code"))
                    .savingText(rs.getString("saving_text"))
                    .badgeText(rs.getString("badge_text"))
                    .bgColorStart(rs.getString("bg_color_start"))
                    .bgColorEnd(rs.getString("bg_color_end"))
                    .imageUrl(rs.getString("image_url"))
                    .discountType(rs.getString("discount_type"))
                    .discountValue(discValStr != null ? new BigDecimal(discValStr) : null)
                    .minOrderAmount(minOrdStr != null ? new BigDecimal(minOrdStr) : null)
                    .maxDiscount(maxDiscStr != null ? new BigDecimal(maxDiscStr) : null)
                    .validUntil(rs.getTimestamp("valid_until") != null
                        ? rs.getTimestamp("valid_until").toInstant().atOffset(java.time.ZoneOffset.UTC)
                        : null)
                    .displayOrder(rs.getInt("display_order"))
                    .build();
            }
        );
        return ResponseEntity.ok(ApiResponse.success(offers));
    }
}
