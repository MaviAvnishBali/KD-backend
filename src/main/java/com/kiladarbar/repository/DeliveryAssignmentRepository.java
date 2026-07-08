package com.kiladarbar.repository;

import com.kiladarbar.model.entity.DeliveryAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, UUID> {

    Optional<DeliveryAssignment> findByIdAndPartnerId(UUID id, UUID partnerId);

    Optional<DeliveryAssignment> findByOrderId(UUID orderId);

    List<DeliveryAssignment> findByPartnerIdAndStatusInOrderByAssignedAtDesc(
            UUID partnerId, List<String> statuses);

    Page<DeliveryAssignment> findByPartnerIdAndStatusOrderByDeliveredAtDesc(
            UUID partnerId, String status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(a.earnings), 0) FROM DeliveryAssignment a " +
           "WHERE a.partner.id = :partnerId AND a.status = 'DELIVERED' " +
           "AND a.deliveredAt >= :from")
    java.math.BigDecimal sumEarningsSince(@Param("partnerId") UUID partnerId,
                                          @Param("from") LocalDateTime from);

    @Query("SELECT COALESCE(SUM(a.earnings), 0) FROM DeliveryAssignment a " +
           "WHERE a.partner.id = :partnerId AND a.status = 'DELIVERED'")
    java.math.BigDecimal sumTotalEarnings(@Param("partnerId") UUID partnerId);

    @Query("SELECT COUNT(a) FROM DeliveryAssignment a " +
           "WHERE a.partner.id = :partnerId AND a.status = 'DELIVERED' " +
           "AND a.deliveredAt >= :from")
    long countDeliveredSince(@Param("partnerId") UUID partnerId,
                             @Param("from") LocalDateTime from);
}
