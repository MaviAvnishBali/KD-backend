package com.kiladarbar.service.impl;

import com.kiladarbar.dto.request.CreateMenuItemRequest;
import com.kiladarbar.dto.request.UpdateMenuItemRequest;
import com.kiladarbar.dto.response.CategoryResponse;
import com.kiladarbar.dto.response.MenuItemResponse;
import com.kiladarbar.exception.ResourceNotFoundException;
import com.kiladarbar.model.entity.ItemImage;
import com.kiladarbar.model.entity.MenuItem;
import com.kiladarbar.model.enums.FoodType;
import com.kiladarbar.repository.BranchRepository;
import com.kiladarbar.repository.CategoryRepository;
import com.kiladarbar.repository.ItemImageRepository;
import com.kiladarbar.repository.MenuItemRepository;
import com.kiladarbar.service.MenuService;
import com.kiladarbar.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MenuServiceImpl implements MenuService {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final BranchRepository branchRepository;
    private final ItemImageRepository itemImageRepository;
    private final S3Service s3Service;

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
    public Page<MenuItemResponse> getAllItems(FoodType foodType, UUID branchId, Pageable pageable) {
        return menuItemRepository.findAllAvailable(foodType, branchId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MenuItemResponse> getAllItemsForAdmin(FoodType foodType, UUID branchId, Pageable pageable) {
        return menuItemRepository.findAllForAdmin(foodType, branchId, pageable).map(this::toResponse);
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
        // branchId is optional — single-branch setups leave menu items unassigned
        var branch = request.getBranchId() == null ? null
                : branchRepository.findById(request.getBranchId())
                        .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        var category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        BigDecimal gstRate = request.getGstRate() != null ? request.getGstRate() : new BigDecimal("5.00");
        MenuItem item = MenuItem.builder()
                .branch(branch).category(category)
                .name(request.getName()).slug(generateSlug(request.getName()))
                .description(request.getDescription())
                .price(request.getPrice()).discountPrice(request.getDiscountPrice())
                .foodType(request.getFoodType()).preparationTime(request.getPreparationTime())
                .gstRate(gstRate)
                .available(true).build();
        return toResponse(menuItemRepository.save(item));
    }

    private static String generateSlug(String name) {
        String base = name == null ? "item" : name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (base.isEmpty()) base = "item";
        return base + "-" + UUID.randomUUID().toString().substring(0, 8);
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
        // Collect stored image URLs before the DB rows cascade away, then delete
        // the underlying files once the delete actually commits.
        List<String> urls = itemImageRepository.findByMenuItemId(id).stream()
                .map(ItemImage::getUrl)
                .collect(Collectors.toList());
        menuItemRepository.deleteById(id);   // item_images rows removed via ON DELETE CASCADE
        deleteFilesAfterCommit(urls);
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
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        // True replace: remove any existing images (rows now, files after commit)
        List<ItemImage> existing = itemImageRepository.findByMenuItemId(id);
        List<String> oldUrls = existing.stream()
                .map(ItemImage::getUrl)
                .collect(Collectors.toList());
        if (!existing.isEmpty()) {
            itemImageRepository.deleteAll(existing);
        }

        String url = s3Service.upload("menu-items/" + id, image);

        itemImageRepository.save(ItemImage.builder()
                .menuItem(item)
                .url(url)
                .primary(true)
                .displayOrder((short) 0)
                .build());

        // Delete the replaced files only once this upload commits
        deleteFilesAfterCommit(oldUrls);
        return url;
    }

    /**
     * Deletes stored files after the current transaction commits (best-effort).
     * Registering on {@code afterCommit} avoids removing objects for a transaction
     * that later rolls back; failures are logged, not propagated.
     */
    private void deleteFilesAfterCommit(List<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    urls.forEach(MenuServiceImpl.this::safeDeleteFile);
                }
            });
        } else {
            urls.forEach(this::safeDeleteFile);
        }
    }

    private void safeDeleteFile(String url) {
        try {
            s3Service.delete(url);
        } catch (Exception e) {
            log.warn("Failed to delete stored image {}: {}", url, e.getMessage());
        }
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
        List<String> imageUrls = item.getImages() == null ? List.of()
                : item.getImages().stream()
                        .sorted((a, b) -> Boolean.compare(b.isPrimary(), a.isPrimary()))
                        .map(ItemImage::getUrl)
                        .collect(Collectors.toList());

        return MenuItemResponse.builder()
                .id(item.getId()).name(item.getName()).slug(item.getSlug())
                .description(item.getDescription()).price(item.getPrice())
                .discountPrice(item.getDiscountPrice()).effectivePrice(item.getEffectivePrice())
                .foodType(item.getFoodType()).isAvailable(item.isAvailable())
                .isBestSeller(item.isBestSeller()).isRecommended(item.isRecommended())
                .isSeasonal(item.isSeasonal()).preparationTime(item.getPreparationTime())
                .calories(item.getCalories()).gstRate(item.getGstRate())
                .imageUrls(imageUrls)
                .categoryId(item.getCategory() != null ? item.getCategory().getId() : null)
                .categoryName(item.getCategory() != null ? item.getCategory().getName() : null)
                .build();
    }
}
