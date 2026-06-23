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
@RequiredArgsConstructor
@Tag(name = "Menu", description = "Browse categories and food items")
public class MenuController {

    private final MenuService menuService;

    /* ── Categories: GET /v1/categories ── */
    @GetMapping({"/v1/categories", "/menu/categories"})
    @Operation(summary = "Get all active categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories(
            @RequestParam(required = false) UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(menuService.getCategories(branchId)));
    }

    /* ── Products: GET /v1/products ── */
    @GetMapping({"/v1/products", "/menu/items"})
    @Operation(summary = "Get menu items with optional filtering")
    public ResponseEntity<ApiResponse<Page<MenuItemResponse>>> getProducts(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) FoodType foodType,
            @RequestParam(required = false) UUID branchId,
            @PageableDefault(size = 20) Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(ApiResponse.success(
                    menuService.search(search, foodType, branchId, pageable)));
        }
        if (categoryId != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    menuService.getItemsByCategory(categoryId, foodType, branchId, pageable)));
        }
        return ResponseEntity.ok(ApiResponse.success(
                menuService.getAllItems(foodType, branchId, pageable)));
    }

    /* ── Best sellers: GET /v1/products/best-sellers ── */
    @GetMapping({"/v1/products/best-sellers", "/menu/best-sellers"})
    @Operation(summary = "Get best seller items")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getBestSellers(
            @RequestParam(required = false) UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(menuService.getBestSellers(branchId)));
    }

    /* ── Recommended: GET /v1/products/recommended ── */
    @GetMapping({"/v1/products/recommended", "/menu/recommended"})
    @Operation(summary = "Get recommended items")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getRecommended(
            @RequestParam(required = false) UUID branchId) {
        return ResponseEntity.ok(ApiResponse.success(menuService.getRecommended(branchId)));
    }

    /* ── Single product: GET /v1/products/{id} ── */
    @GetMapping({"/v1/products/{id}", "/menu/items/{id}"})
    @Operation(summary = "Get item detail by ID")
    public ResponseEntity<ApiResponse<MenuItemResponse>> getItem(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(menuService.getItemById(id)));
    }

    /* ── Category items (legacy): GET /menu/categories/{id}/items ── */
    @GetMapping("/menu/categories/{id}/items")
    @Operation(summary = "Get items by category (legacy)")
    public ResponseEntity<ApiResponse<Page<MenuItemResponse>>> getItemsByCategory(
            @PathVariable Integer id,
            @RequestParam(required = false) FoodType foodType,
            @RequestParam(required = false) UUID branchId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                menuService.getItemsByCategory(id, foodType, branchId, pageable)));
    }
}
