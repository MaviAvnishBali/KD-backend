package com.kiladarbar.service;

import com.kiladarbar.dto.request.CreatePartyHallBookingRequest;
import com.kiladarbar.dto.response.PartyHallBookingResponse;

import java.util.List;
import java.util.UUID;

public interface PartyHallBookingService {
    PartyHallBookingResponse create(UUID userId, CreatePartyHallBookingRequest request);
    List<PartyHallBookingResponse> getUserBookings(UUID userId);
    void cancel(UUID userId, UUID bookingId);
}
