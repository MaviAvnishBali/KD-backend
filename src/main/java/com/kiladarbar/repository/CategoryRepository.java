package com.kiladarbar.repository;

import com.kiladarbar.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByBranchIdAndActiveTrueOrderByDisplayOrderAsc(UUID branchId);
    List<Category> findByActiveTrueOrderByDisplayOrderAsc();
}
