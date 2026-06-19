package com.kiladarbar.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(unique = true, nullable = false, length = 50)
    private String sku;

    @Column(length = 50)
    private String category;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(name = "current_stock", nullable = false, precision = 12, scale = 4)
    private BigDecimal currentStock = BigDecimal.ZERO;

    @Column(name = "reorder_level", nullable = false, precision = 12, scale = 4)
    private BigDecimal reorderLevel = BigDecimal.ZERO;

    @Column(name = "reorder_quantity", precision = 12, scale = 4)
    private BigDecimal reorderQuantity;

    @Column(name = "cost_per_unit", precision = 10, scale = 4)
    private BigDecimal costPerUnit;

    @Column(name = "storage_location", length = 50)
    private String storageLocation;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isLowStock() {
        return currentStock.compareTo(reorderLevel) <= 0;
    }
}
