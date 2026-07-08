package com.kiladarbar.controller.admin;

import com.kiladarbar.dto.request.CreateMenuItemRequest;
import com.kiladarbar.dto.request.UpdateMenuItemRequest;
import com.kiladarbar.dto.response.ApiResponse;
import com.kiladarbar.dto.response.MenuItemResponse;
import com.kiladarbar.model.enums.FoodType;
import com.kiladarbar.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/admin/menu")
@RequiredArgsConstructor
@Tag(name = "Admin - Menu", description = "Menu management")
@PreAuthorize("hasAnyRole('MANAGER', 'OWNER', 'SUPER_ADMIN')")
public class AdminMenuController {

    private final MenuService menuService;

    @GetMapping("/items")
    @Operation(summary = "List all menu items (includes hidden/unavailable) for management")
    public ResponseEntity<ApiResponse<Page<MenuItemResponse>>> listItems(
            @RequestParam(required = false) FoodType foodType,
            @RequestParam(required = false) UUID branchId,
            @PageableDefault(size = 100) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                menuService.getAllItemsForAdmin(foodType, branchId, pageable)));
    }

    @PostMapping("/items")
    @Operation(summary = "Create a new menu item")
    public ResponseEntity<ApiResponse<MenuItemResponse>> createItem(
            @Valid @RequestBody CreateMenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(menuService.createItem(request)));
    }

    @PutMapping("/items/{id}")
    @Operation(summary = "Update a menu item")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateItem(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMenuItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(menuService.updateItem(id, request)));
    }

    @DeleteMapping("/items/{id}")
    @Operation(summary = "Delete a menu item")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable UUID id) {
        menuService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.success("Item deleted successfully"));
    }

    @PatchMapping("/items/{id}/toggle-availability")
    @Operation(summary = "Toggle item availability")
    public ResponseEntity<ApiResponse<MenuItemResponse>> toggleAvailability(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(menuService.toggleAvailability(id)));
    }

    @PostMapping(value = "/items/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload item image")
    public ResponseEntity<ApiResponse<String>> uploadImage(
            @PathVariable UUID id,
            @RequestPart("image") MultipartFile image) {
        String imageUrl = menuService.uploadItemImage(id, image);
        return ResponseEntity.ok(ApiResponse.success(imageUrl));
    }

    @PatchMapping("/items/{id}/display-order")
    @Operation(summary = "Update display order")
    public ResponseEntity<ApiResponse<Void>> updateDisplayOrder(
            @PathVariable UUID id,
            @RequestParam int order) {
        menuService.updateDisplayOrder(id, order);
        return ResponseEntity.ok(ApiResponse.success("Display order updated"));
    }

    @PostMapping("/categories")
    @Operation(summary = "Create a category")
    public ResponseEntity<ApiResponse<?>> createCategory(@Valid @RequestBody Object request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(menuService.createCategory(request)));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<?>> updateCategory(
            @PathVariable Integer id,
            @RequestBody Object request) {
        return ResponseEntity.ok(ApiResponse.success(menuService.updateCategory(id, request)));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Integer id) {
        menuService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted"));
    }
}
