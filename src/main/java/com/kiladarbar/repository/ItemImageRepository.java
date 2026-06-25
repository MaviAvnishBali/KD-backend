package com.kiladarbar.repository;

import com.kiladarbar.model.entity.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {

    @Modifying
    @Query("UPDATE ItemImage i SET i.primary = false WHERE i.menuItem.id = :itemId")
    void clearPrimary(UUID itemId);
}
