package com.kiladarbar.repository;

import com.kiladarbar.model.entity.PartyHallBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PartyHallBookingRepository extends JpaRepository<PartyHallBooking, UUID> {

    List<PartyHallBooking> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
