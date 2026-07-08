package com.kiladarbar.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_assignments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeliveryAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private DeliveryPartner partner;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "delivery_distance_km", precision = 6, scale = 2)
    private BigDecimal deliveryDistanceKm;

    @Column(name = "delivery_duration_min")
    private Short deliveryDurationMin;

    @Column(name = "earnings", precision = 10, scale = 2)
    private BigDecimal earnings;

    @Column(name = "delivery_otp", length = 6)
    private String deliveryOtp;

    /** ASSIGNED → ACCEPTED → PICKED_UP → DELIVERED (or REJECTED) */
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ASSIGNED";
}
