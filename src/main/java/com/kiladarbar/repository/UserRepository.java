package com.kiladarbar.repository;

import com.kiladarbar.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleId(String googleId);
    boolean existsByPhone(String phone);

    @Query("SELECT u FROM User u LEFT JOIN u.loyaltyAccount la " +
           "WHERE u.guest = false AND (:tier IS NULL OR la.tier = :tier)")
    Page<User> findCustomers(@Param("tier") String tier, Pageable pageable);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE User u SET u.fcmToken = :token WHERE u.id = :id")
    void updateFcmToken(@org.springframework.data.repository.query.Param("id") UUID id,
                        @org.springframework.data.repository.query.Param("token") String token);
}
