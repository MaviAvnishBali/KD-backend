package com.kiladarbar.service;

import com.kiladarbar.dto.request.CreateMenuItemRequest;
import com.kiladarbar.dto.request.UpdateMenuItemRequest;
import com.kiladarbar.dto.response.CategoryResponse;
import com.kiladarbar.dto.response.MenuItemResponse;
import com.kiladarbar.model.enums.FoodType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

public interface MenuService {
    List<CategoryResponse> getCategories(UUID branchId);
    Page<MenuItemResponse> getItemsByCategory(Integer categoryId, FoodType foodType, UUID branchId, Pageable pageable);
    MenuItemResponse getItemById(UUID id);
    Page<MenuItemResponse> search(String query, FoodType foodType, UUID branchId, Pageable pageable);
    List<MenuItemResponse> getBestSellers(UUID branchId);
    List<MenuItemResponse> getRecommended(UUID branchId);
    List<MenuItemResponse> getCombos(UUID branchId);
    List<MenuItemResponse> getSeasonalSpecials(UUID branchId);
    MenuItemResponse createItem(CreateMenuItemRequest request);
    MenuItemResponse updateItem(UUID id, UpdateMenuItemRequest request);
    void deleteItem(UUID id);
    MenuItemResponse toggleAvailability(UUID id);
    String uploadItemImage(UUID id, MultipartFile image);
    void updateDisplayOrder(UUID id, int order);
    Object createCategory(Object request);
    Object updateCategory(Integer id, Object request);
    void deleteCategory(Integer id);
}
