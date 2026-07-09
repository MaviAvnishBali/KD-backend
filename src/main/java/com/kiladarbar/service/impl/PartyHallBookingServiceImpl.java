package com.kiladarbar.service.impl;

import com.kiladarbar.dto.request.CreatePartyHallBookingRequest;
import com.kiladarbar.dto.response.PartyHallBookingResponse;
import com.kiladarbar.exception.BusinessException;
import com.kiladarbar.exception.ResourceNotFoundException;
import com.kiladarbar.model.entity.PartyHallBooking;
import com.kiladarbar.model.entity.User;
import com.kiladarbar.repository.PartyHallBookingRepository;
import com.kiladarbar.repository.UserRepository;
import com.kiladarbar.service.PartyHallBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PartyHallBookingServiceImpl implements PartyHallBookingService {

    private final PartyHallBookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    public PartyHallBookingResponse create(UUID userId, CreatePartyHallBookingRequest req) {
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

        PartyHallBooking booking = PartyHallBooking.builder()
                .user(user)
                .customerName(req.getCustomerName())
                .customerPhone(req.getCustomerPhone())
                .customerEmail(req.getCustomerEmail())
                .eventType(req.getEventType())
                .guestCount(req.getGuestCount())
                .preferredDate(req.getPreferredDate())
                .preferredTime(req.getPreferredTime())
                .packageType(req.getPackageType())
                .specialRequests(req.getSpecialRequests())
                .status("PENDING")   // inquiry — staff confirm pricing later
                .build();

        booking = bookingRepository.save(booking);
        log.info("Party hall booking {} created for {} ({} guests, {})",
                booking.getId(), req.getCustomerName(), req.getGuestCount(), req.getPreferredDate());

        return mapToResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartyHallBookingResponse> getUserBookings(UUID userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void cancel(UUID userId, UUID bookingId) {
        PartyHallBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getUser() == null || !booking.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Booking not found");
        }
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new BusinessException("Booking is already cancelled");
        }

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
        log.info("Party hall booking {} cancelled by user {}", bookingId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartyHallBookingResponse> adminList(String status) {
        List<PartyHallBooking> bookings = (status == null || status.isBlank())
                ? bookingRepository.findAllByOrderByCreatedAtDesc()
                : bookingRepository.findByStatusOrderByCreatedAtDesc(status);
        return bookings.stream().map(this::mapToResponse).toList();
    }

    @Override
    public PartyHallBookingResponse adminUpdateStatus(UUID bookingId, String status) {
        PartyHallBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        booking.setStatus(status);
        booking = bookingRepository.save(booking);
        log.info("Party hall booking {} moved to {} by admin", bookingId, status);
        return mapToResponse(booking);
    }

    @Override
    public PartyHallBookingResponse adminUpdatePackage(UUID bookingId, String packageType, java.math.BigDecimal totalAmount) {
        PartyHallBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (packageType != null && !packageType.isBlank()) booking.setPackageType(packageType);
        booking.setTotalAmount(totalAmount);
        booking = bookingRepository.save(booking);
        log.info("Party hall booking {} package set to {} (₹{}) by admin", bookingId, booking.getPackageType(), totalAmount);
        return mapToResponse(booking);
    }

    private PartyHallBookingResponse mapToResponse(PartyHallBooking b) {
        return PartyHallBookingResponse.builder()
                .id(b.getId())
                .customerName(b.getCustomerName())
                .customerPhone(b.getCustomerPhone())
                .customerEmail(b.getCustomerEmail())
                .eventType(b.getEventType())
                .guestCount(b.getGuestCount())
                .preferredDate(b.getPreferredDate())
                .preferredTime(b.getPreferredTime())
                .packageType(b.getPackageType())
                .specialRequests(b.getSpecialRequests())
                .status(b.getStatus())
                .totalAmount(b.getTotalAmount())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
