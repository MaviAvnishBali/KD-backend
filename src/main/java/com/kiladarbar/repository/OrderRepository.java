package com.kiladarbar.repository;

import com.kiladarbar.model.entity.Order;
import com.kiladarbar.model.enums.OrderStatus;
import com.kiladarbar.model.enums.OrderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByIdAndUserId(UUID id, UUID userId);
    Page<Order> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    boolean existsByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o WHERE " +
           "(:branchId IS NULL OR o.branch.id = :branchId) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:type IS NULL OR o.orderType = :type) AND " +
           "(:from IS NULL OR CAST(o.createdAt AS LocalDate) >= :from) AND " +
           "(:to IS NULL OR CAST(o.createdAt AS LocalDate) <= :to)")
    Page<Order> findWithFilters(@Param("branchId") UUID branchId,
                                @Param("status") OrderStatus status,
                                @Param("type") OrderType type,
                                @Param("from") LocalDate from,
                                @Param("to") LocalDate to,
                                Pageable pageable);
}
