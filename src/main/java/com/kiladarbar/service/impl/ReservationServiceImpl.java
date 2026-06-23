package com.kiladarbar.service.impl;

import com.kiladarbar.dto.request.CreateReservationRequest;
import com.kiladarbar.dto.response.ReservationResponse;
import com.kiladarbar.exception.BusinessException;
import com.kiladarbar.exception.ResourceNotFoundException;
import com.kiladarbar.model.entity.Branch;
import com.kiladarbar.model.entity.Reservation;
import com.kiladarbar.model.entity.RestaurantTable;
import com.kiladarbar.model.entity.User;
import com.kiladarbar.repository.BranchRepository;
import com.kiladarbar.repository.OrderRepository;
import com.kiladarbar.repository.UserRepository;
import com.kiladarbar.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;

    // Available dinner/lunch slots
    private static final List<LocalTime> AVAILABLE_TIMES = List.of(
            LocalTime.of(11, 0), LocalTime.of(12, 30), LocalTime.of(14, 0),
            LocalTime.of(16, 0), LocalTime.of(18, 0), LocalTime.of(19, 30),
            LocalTime.of(21, 0), LocalTime.of(22, 30)
    );

    @Override
    public ReservationResponse create(UUID userId, CreateReservationRequest req) {
        Branch branch = branchRepository.findById(req.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        if (req.getReservedDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Cannot book a table in the past");
        }

        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

        Reservation reservation = Reservation.builder()
                .branch(branch)
                .user(user)
                .customerName(req.getCustomerName())
                .customerPhone(req.getCustomerPhone())
                .partySize((short) req.getPartySize())
                .reservedDate(req.getReservedDate())
                .reservedTime(req.getReservedTime())
                .occasion(req.getOccasion())
                .specialRequest(req.getSpecialRequest())
                .status("CONFIRMED")
                .build();

        // In a real system, query and assign a table here
        log.info("Reservation created for {} on {} at {}", req.getCustomerName(), req.getReservedDate(), req.getReservedTime());

        return mapToResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getUserReservations(UUID userId) {
        return List.of(); // TODO: fetch from repository
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getById(UUID id) {
        throw new ResourceNotFoundException("Reservation not found");
    }

    @Override
    public void cancel(UUID userId, UUID id) {
        log.info("Cancelling reservation {} for user {}", id, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableSlots(UUID branchId, LocalDate date, int partySize) {
        // Return all time slots — real implementation would subtract booked ones
        return AVAILABLE_TIMES.stream()
                .map(LocalTime::toString)
                .collect(Collectors.toList());
    }

    private ReservationResponse mapToResponse(Reservation r) {
        return ReservationResponse.builder()
                .id(r.getId())
                .branchName(r.getBranch() != null ? r.getBranch().getName() : null)
                .customerName(r.getCustomerName())
                .customerPhone(r.getCustomerPhone())
                .partySize(r.getPartySize())
                .reservedDate(r.getReservedDate())
                .reservedTime(r.getReservedTime())
                .occasion(r.getOccasion())
                .specialRequest(r.getSpecialRequest())
                .status(r.getStatus())
                .build();
    }
}
