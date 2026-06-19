package com.kiladarbar.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "item_images")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private MenuItem menuItem;

    @Column(nullable = false)
    private String url;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;

    @Column(name = "display_order", nullable = false)
    private short displayOrder = 0;
}
