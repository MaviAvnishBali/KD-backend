package com.kiladarbar.controller.customer;

import com.kiladarbar.dto.response.ApiResponse;
import com.kiladarbar.dto.response.CategoryResponse;
import com.kiladarbar.dto.response.MenuItemResponse;
import com.kiladarbar.model.enums.FoodType;
import com.kiladarbar.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
@Tag(name = "Menu", description = "Browse categories and food items")
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/categories")
    @Operation(summary = "Get all active categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories(
            @RequestParam(required = false) UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(menuService.getCategories(branchId)));
    }

    @GetMapping("/categories/{id}/items")
    @Operation(summary = "Get items by category")
    public ResponseEntity<ApiResponse<Page<MenuItemResponse>>> getItemsByCategory(
            @PathVariable Integer id,
            @RequestParam(required = false) FoodType foodType,
            @RequestParam(required = false) UUID branchId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                menuService.getItemsByCategory(id, foodType, branchId, pageable)));
    }

    @GetMapping("/items/{id}")
    @Operation(summary = "Get item detail")
    public ResponseEntity<ApiResponse<MenuItemResponse>> getItem(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(menuService.getItemById(id)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search menu items")
    public ResponseEntity<ApiResponse<Page<MenuItemResponse>>> search(
            @RequestParam String q,
            @RequestParam(required = false) FoodType foodType,
            @RequestParam(required = false) UUID branchId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                menuService.search(q, foodType, branchId, pageable)));
    }

    @GetMapping("/best-sellers")
    @Operation(summary = "Get best seller items")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getBestSellers(
            @RequestParam(required = false) UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(menuService.getBestSellers(branchId)));
    }

    @GetMapping("/recommended")
    @Operation(summary = "Get recommended items")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getRecommended(
            @RequestParam(required = false) UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(menuService.getRecommended(branchId)));
    }

    @GetMapping("/combos")
    @Operation(summary = "Get combo meals")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getCombos(
            @RequestParam(required = false) UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(menuService.getCombos(branchId)));
    }

    @GetMapping("/seasonal")
    @Operation(summary = "Get seasonal specials")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getSeasonalSpecials(
            @RequestParam(required = false) UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(menuService.getSeasonalSpecials(branchId)));
    }
}
