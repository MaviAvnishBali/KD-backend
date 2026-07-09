package com.kiladarbar.repository;

import com.kiladarbar.model.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    List<Reservation> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Reservation> findAllByOrderByCreatedAtDesc();

    List<Reservation> findByStatusOrderByCreatedAtDesc(String status);
}
