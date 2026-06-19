package com.kiladarbar.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "restaurant_tables")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RestaurantTable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "table_number", nullable = false, length = 10)
    private String tableNumber;

    @Column(nullable = false)
    private short capacity;

    @Column(length = 50)
    private String section;

    @Column(name = "qr_code")
    private String qrCode;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
