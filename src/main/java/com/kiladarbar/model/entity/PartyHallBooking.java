package com.kiladarbar.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "party_hall_bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PartyHallBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "customer_phone", nullable = false, length = 15)
    private String customerPhone;

    @Column(name = "customer_email", length = 150)
    private String customerEmail;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "guest_count", nullable = false)
    private int guestCount;

    // Stored as free text — the app collects these as loose strings.
    @Column(name = "preferred_date", nullable = false, length = 30)
    private String preferredDate;

    @Column(name = "preferred_time", nullable = false, length = 30)
    private String preferredTime;

    @Column(name = "package_type", nullable = false, length = 30)
    private String packageType;

    @Column(name = "special_requests")
    private String specialRequests;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
