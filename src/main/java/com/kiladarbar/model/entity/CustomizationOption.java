package com.kiladarbar.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "customization_options")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomizationOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private CustomizationGroup group;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "additional_price", nullable = false, precision = 8, scale = 2)
    private BigDecimal additionalPrice = BigDecimal.ZERO;

    @Column(name = "is_default", nullable = false)
    private boolean defaultOption = false;

    @Column(name = "is_available", nullable = false)
    private boolean available = true;
}
