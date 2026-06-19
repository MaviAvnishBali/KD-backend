package com.kiladarbar.repository;

import com.kiladarbar.model.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Integer> {
    @Query("SELECT c FROM Coupon c WHERE c.code = :code AND c.active = true AND " +
           "c.startDate <= CURRENT_TIMESTAMP AND c.endDate >= CURRENT_TIMESTAMP")
    Optional<Coupon> findActiveByCode(@Param("code") String code);
}
