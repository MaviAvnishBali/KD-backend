package com.kiladarbar.service.impl;

import com.kiladarbar.dto.request.CreateMenuItemRequest;
import com.kiladarbar.dto.request.UpdateMenuItemRequest;
import com.kiladarbar.dto.response.CategoryResponse;
import com.kiladarbar.dto.response.MenuItemResponse;
import com.kiladarbar.exception.ResourceNotFoundException;
import com.kiladarbar.model.entity.MenuItem;
import com.kiladarbar.model.enums.FoodType;
import com.kiladarbar.repository.BranchRepository;
import com.kiladarbar.repository.CategoryRepository;
import com.kiladarbar.repository.MenuItemRepository;
import com.kiladarbar.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuServiceImpl implements MenuService {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final BranchRepository branchRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(UUID branchId) {
        var cats = branchId != null
                ? categoryRepository.findByBranchIdAndActiveTrueOrderByDisplayOrderAsc(branchId)
                : categoryRepository.findByActiveTrueOrderByDisplayOrderAsc();
        return cats.stream().map(c -> CategoryResponse.builder()
                .id(c.getId()).name(c.getName()).slug(c.getSlug())
                .description(c.getDescription()).imageUrl(c.getImageUrl())
                .displayOrder(c.getDisplayOrder()).build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MenuItemResponse> getItemsByCategory(Integer categoryId, FoodType foodType,
                                                      UUID branchId, Pageable pageable) {
        Page<MenuItem> items = foodType != null
                ? menuItemRepository.findByCategoryIdAndAvailableTrueAndFoodTypeAndBranchId(categoryId, foodType, branchId, pageable)
                : menuItemRepository.findByCategoryIdAndAvailableTrueAndBranchId(categoryId, branchId, pageable);
        return items.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuItemResponse getItemById(UUID id) {
        return toResponse(menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MenuItemResponse> search(String query, FoodType foodType, UUID branchId, Pageable pageable) {
        Page<MenuItem> items = foodType != null
                ? menuItemRepository.searchWithFoodType(query, foodType, branchId, pageable)
                : menuItemRepository.search(query, branchId, pageable);
        return items.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getBestSellers(UUID branchId) {
        return menuItemRepository.findByBestSellerTrueAndAvailableTrueAndBranchId(branchId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getRecommended(UUID branchId) {
        return menuItemRepository.findByRecommendedTrueAndAvailableTrueAndBranchId(branchId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getCombos(UUID branchId) {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getSeasonalSpecials(UUID branchId) {
        return menuItemRepository.findBySeasonalTrueAndAvailableTrueAndBranchId(branchId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public MenuItemResponse createItem(CreateMenuItemRequest request) {
        var branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        var category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        MenuItem item = MenuItem.builder()
                .branch(branch).category(category)
                .name(request.getName()).description(request.getDescription())
                .price(request.getPrice()).discountPrice(request.getDiscountPrice())
                .foodType(request.getFoodType()).preparationTime(request.getPreparationTime())
                .available(true).build();
        return toResponse(menuItemRepository.save(item));
    }

    @Override
    public MenuItemResponse updateItem(UUID id, UpdateMenuItemRequest request) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        if (request.getName() != null) item.setName(request.getName());
        if (request.getPrice() != null) item.setPrice(request.getPrice());
        if (request.getDiscountPrice() != null) item.setDiscountPrice(request.getDiscountPrice());
        if (request.getIsAvailable() != null) item.setAvailable(request.getIsAvailable());
        return toResponse(menuItemRepository.save(item));
    }

    @Override
    public void deleteItem(UUID id) {
        menuItemRepository.deleteById(id);
    }

    @Override
    public MenuItemResponse toggleAvailability(UUID id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        item.setAvailable(!item.isAvailable());
        return toResponse(menuItemRepository.save(item));
    }

    @Override
    public String uploadItemImage(UUID id, MultipartFile image) {
        // In prod: upload to S3 and return URL
        return "/images/placeholder.jpg";
    }

    @Override
    public void updateDisplayOrder(UUID id, int order) {
        menuItemRepository.findById(id).ifPresent(item -> {
            item.setDisplayOrder(order);
            menuItemRepository.save(item);
        });
    }

    @Override
    public Object createCategory(Object request) { return request; }

    @Override
    public Object updateCategory(Integer id, Object request) { return request; }

    @Override
    public void deleteCategory(Integer id) {
        categoryRepository.deleteById(id);
    }

    private MenuItemResponse toResponse(MenuItem item) {
        return MenuItemResponse.builder()
                .id(item.getId()).name(item.getName()).slug(item.getSlug())
                .description(item.getDescription()).price(item.getPrice())
                .discountPrice(item.getDiscountPrice()).effectivePrice(item.getEffectivePrice())
                .foodType(item.getFoodType()).isAvailable(item.isAvailable())
                .isBestSeller(item.isBestSeller()).isRecommended(item.isRecommended())
                .isSeasonal(item.isSeasonal()).preparationTime(item.getPreparationTime())
                .calories(item.getCalories()).gstRate(item.getGstRate())
                .categoryId(item.getCategory() != null ? item.getCategory().getId() : null)
                .categoryName(item.getCategory() != null ? item.getCategory().getName() : null)
                .build();
    }
}
