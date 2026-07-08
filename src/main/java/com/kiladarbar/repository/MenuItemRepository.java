package com.kiladarbar.repository;

import com.kiladarbar.model.entity.MenuItem;
import com.kiladarbar.model.enums.FoodType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
    Page<MenuItem> findByCategoryIdAndAvailableTrueAndBranchId(
            Integer categoryId, UUID branchId, Pageable pageable);
    Page<MenuItem> findByCategoryIdAndAvailableTrueAndFoodTypeAndBranchId(
            Integer categoryId, FoodType foodType, UUID branchId, Pageable pageable);

    @Query("SELECT m FROM MenuItem m WHERE m.available = true AND " +
           "(:branchId IS NULL OR m.branch.id = :branchId) AND " +
           "(LOWER(m.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(m.description) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<MenuItem> search(@Param("q") String query, @Param("branchId") UUID branchId, Pageable pageable);

    @Query("SELECT m FROM MenuItem m WHERE m.available = true AND " +
           "(:branchId IS NULL OR m.branch.id = :branchId) AND " +
           "m.foodType = :foodType AND " +
           "(LOWER(m.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(m.description) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<MenuItem> searchWithFoodType(@Param("q") String query, @Param("foodType") FoodType foodType,
                                       @Param("branchId") UUID branchId, Pageable pageable);

    @Query("SELECT m FROM MenuItem m WHERE m.available = true AND (:branchId IS NULL OR m.branch.id = :branchId) AND (:foodType IS NULL OR m.foodType = :foodType) ORDER BY m.displayOrder ASC")
    Page<MenuItem> findAllAvailable(@Param("foodType") FoodType foodType, @Param("branchId") UUID branchId, Pageable pageable);

    /** Admin listing — includes hidden (unavailable) items so they can be managed / re-enabled. */
    @Query("SELECT m FROM MenuItem m WHERE (:branchId IS NULL OR m.branch.id = :branchId) AND (:foodType IS NULL OR m.foodType = :foodType) ORDER BY m.displayOrder ASC")
    Page<MenuItem> findAllForAdmin(@Param("foodType") FoodType foodType, @Param("branchId") UUID branchId, Pageable pageable);

    List<MenuItem> findByBestSellerTrueAndAvailableTrueAndBranchId(UUID branchId);
    List<MenuItem> findByRecommendedTrueAndAvailableTrueAndBranchId(UUID branchId);
    List<MenuItem> findBySeasonalTrueAndAvailableTrueAndBranchId(UUID branchId);
}
