package com.kiladarbar.controller.admin;

import com.kiladarbar.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/banners")
@RequiredArgsConstructor
@Tag(name = "Admin - Banners & Offers", description = "Manage hero banners and promotional offers")
@PreAuthorize("hasAnyRole('MANAGER', 'OWNER', 'SUPER_ADMIN')")
public class AdminBannerController {

    private final JdbcTemplate jdbc;

    // ── Banners ───────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List all banners")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listBanners() {
        var rows = jdbc.queryForList(
            "SELECT id, title, subtitle, tag, emoji, " +
            "       bg_color_start AS \"bgColorStart\", bg_color_end AS \"bgColorEnd\", " +
            "       cta_text AS \"ctaText\", cta_link AS \"ctaLink\", " +
            "       display_order AS \"displayOrder\", is_active FROM banners ORDER BY display_order");
        return ResponseEntity.ok(ApiResponse.success(rows));
    }

    @PostMapping
    @Operation(summary = "Create a banner slide")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createBanner(@RequestBody Map<String, Object> body) {
        UUID id = UUID.randomUUID();
        jdbc.update(
            "INSERT INTO banners (id, title, subtitle, tag, emoji, bg_color_start, bg_color_end, " +
            "  cta_text, cta_link, display_order, is_active) VALUES (?,?,?,?,?,?,?,?,?,?,true)",
            id.toString(),
            body.get("title"), body.get("subtitle"), body.get("tag"), body.get("emoji"),
            body.getOrDefault("bgColorStart", "#6B0F1A"),
            body.getOrDefault("bgColorEnd",   "#111111"),
            body.get("ctaText"), body.get("ctaLink"),
            body.getOrDefault("displayOrder", 99)
        );
        body.put("id", id.toString());
        return ResponseEntity.ok(ApiResponse.success(body, "Banner created"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a banner")
    public ResponseEntity<ApiResponse<Void>> updateBanner(@PathVariable UUID id,
                                                           @RequestBody Map<String, Object> body) {
        jdbc.update(
            "UPDATE banners SET title=?, subtitle=?, tag=?, emoji=?, " +
            "  bg_color_start=?, bg_color_end=?, cta_text=?, cta_link=?, display_order=? WHERE id=?",
            body.get("title"), body.get("subtitle"), body.get("tag"), body.get("emoji"),
            body.get("bgColorStart"), body.get("bgColorEnd"),
            body.get("ctaText"), body.get("ctaLink"), body.get("displayOrder"),
            id.toString()
        );
        return ResponseEntity.ok(ApiResponse.success("Banner updated"));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle banner active state")
    public ResponseEntity<ApiResponse<Void>> toggleBanner(@PathVariable UUID id) {
        jdbc.update("UPDATE banners SET is_active = NOT is_active WHERE id = ?", id.toString());
        return ResponseEntity.ok(ApiResponse.success("Toggled"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBanner(@PathVariable UUID id) {
        jdbc.update("DELETE FROM banners WHERE id = ?", id.toString());
        return ResponseEntity.ok(ApiResponse.success("Banner deleted"));
    }

    // ── Offers ────────────────────────────────────────────────────────────────

    @GetMapping("/offers")
    @Operation(summary = "List all offers")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listOffers() {
        var rows = jdbc.queryForList(
            "SELECT id, emoji, title, description, " +
            "  promo_code AS \"promoCode\", saving_text AS \"savingText\", badge_text AS \"badgeText\", " +
            "  bg_color_start AS \"bgColorStart\", bg_color_end AS \"bgColorEnd\", " +
            "  discount_type AS \"discountType\", discount_value AS \"discountValue\", " +
            "  min_order_amount AS \"minOrderAmount\", max_discount AS \"maxDiscount\", " +
            "  valid_until AS \"validUntil\", display_order AS \"displayOrder\", is_active " +
            "FROM offer_banners ORDER BY display_order");
        return ResponseEntity.ok(ApiResponse.success(rows));
    }

    @PostMapping("/offers")
    @Operation(summary = "Create a promotional offer")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createOffer(@RequestBody Map<String, Object> body) {
        UUID id = UUID.randomUUID();
        String discountType  = (String) body.get("discountType");
        Object discountValue = body.get("discountValue");
        Object minOrder      = body.get("minOrderAmount");
        Object maxDiscount   = body.get("maxDiscount");
        String validUntil    = (String) body.get("validUntil");

        jdbc.update(
            "INSERT INTO offer_banners (id, emoji, title, description, promo_code, saving_text, badge_text, " +
            "  bg_color_start, bg_color_end, discount_type, discount_value, min_order_amount, " +
            "  max_discount, valid_until, display_order, is_active) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,true)",
            id.toString(),
            body.getOrDefault("emoji", "🎁"),
            body.get("title"), body.get("description"), body.get("promoCode"),
            body.get("savingText"), body.get("badgeText"),
            body.getOrDefault("bgColorStart", "#6B0F1A"),
            body.getOrDefault("bgColorEnd",   "#3d0409"),
            discountType,
            discountValue != null ? new BigDecimal(discountValue.toString()) : null,
            minOrder      != null ? new BigDecimal(minOrder.toString())      : null,
            maxDiscount   != null ? new BigDecimal(maxDiscount.toString())   : null,
            validUntil != null ? java.sql.Timestamp.valueOf(validUntil.replace("T", " ").replace("Z", "")) : null,
            body.getOrDefault("displayOrder", 99)
        );
        body.put("id", id.toString());
        return ResponseEntity.ok(ApiResponse.success(body, "Offer created"));
    }

    @PutMapping("/offers/{id}")
    @Operation(summary = "Update a promotional offer")
    public ResponseEntity<ApiResponse<Void>> updateOffer(@PathVariable UUID id,
                                                          @RequestBody Map<String, Object> body) {
        Object discountValue = body.get("discountValue");
        Object minOrder      = body.get("minOrderAmount");
        Object maxDiscount   = body.get("maxDiscount");
        String validUntil    = (String) body.get("validUntil");

        jdbc.update(
            "UPDATE offer_banners SET emoji=?, title=?, description=?, promo_code=?, saving_text=?, badge_text=?, " +
            "  bg_color_start=?, bg_color_end=?, discount_type=?, discount_value=?, min_order_amount=?, " +
            "  max_discount=?, valid_until=?, display_order=? WHERE id=?",
            body.get("emoji"), body.get("title"), body.get("description"), body.get("promoCode"),
            body.get("savingText"), body.get("badgeText"),
            body.get("bgColorStart"), body.get("bgColorEnd"), body.get("discountType"),
            discountValue != null ? new BigDecimal(discountValue.toString()) : null,
            minOrder      != null ? new BigDecimal(minOrder.toString())      : null,
            maxDiscount   != null ? new BigDecimal(maxDiscount.toString())   : null,
            validUntil != null ? java.sql.Timestamp.valueOf(validUntil.replace("T", " ").replace("Z", "")) : null,
            body.get("displayOrder"),
            id.toString()
        );
        return ResponseEntity.ok(ApiResponse.success("Offer updated"));
    }

    @PatchMapping("/offers/{id}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleOffer(@PathVariable UUID id) {
        jdbc.update("UPDATE offer_banners SET is_active = NOT is_active WHERE id = ?", id.toString());
        return ResponseEntity.ok(ApiResponse.success("Toggled"));
    }

    @DeleteMapping("/offers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOffer(@PathVariable UUID id) {
        jdbc.update("DELETE FROM offer_banners WHERE id = ?", id.toString());
        return ResponseEntity.ok(ApiResponse.success("Offer deleted"));
    }
}
