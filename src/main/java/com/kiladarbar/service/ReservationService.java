package com.kiladarbar.service;

import com.kiladarbar.dto.request.CreateReservationRequest;
import com.kiladarbar.dto.response.ReservationResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReservationService {
    ReservationResponse create(UUID userId, CreateReservationRequest request);
    List<ReservationResponse> getUserReservations(UUID userId);
    ReservationResponse getById(UUID id);
    void cancel(UUID userId, UUID id);
    List<String> getAvailableSlots(UUID branchId, LocalDate date, int partySize);

    // ── Admin ──
    List<ReservationResponse> adminList(String status);
    ReservationResponse adminConfirm(UUID id);
    void adminDelete(UUID id);
}
