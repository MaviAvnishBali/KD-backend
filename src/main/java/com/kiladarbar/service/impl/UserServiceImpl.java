package com.kiladarbar.service.impl;

import com.kiladarbar.dto.request.CreateAddressRequest;
import com.kiladarbar.dto.request.UpdateProfileRequest;
import com.kiladarbar.dto.response.AddressResponse;
import com.kiladarbar.dto.response.UserProfileResponse;
import com.kiladarbar.exception.ResourceNotFoundException;
import com.kiladarbar.model.entity.User;
import com.kiladarbar.model.entity.UserAddress;
import com.kiladarbar.model.enums.Gender;
import com.kiladarbar.repository.UserRepository;
import com.kiladarbar.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        return mapToResponse(findUser(userId));
    }

    @Override
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest req) {
        User user = findUser(userId);
        if (req.getName()        != null) user.setName(req.getName());
        if (req.getEmail()       != null) user.setEmail(req.getEmail());
        if (req.getDateOfBirth() != null) user.setDateOfBirth(req.getDateOfBirth());
        if (req.getAvatarUrl()   != null) user.setAvatarUrl(req.getAvatarUrl());
        if (req.getAnniversaryDate() != null) user.setAnniversaryDate(req.getAnniversaryDate());
        if (req.getGender() != null) {
            try { user.setGender(Gender.valueOf(req.getGender().toUpperCase())); }
            catch (IllegalArgumentException ignored) {}
        }
        return mapToResponse(userRepository.save(user));
    }

    @Override
    public void deleteAccount(UUID userId) {
        User user = findUser(userId);
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses(UUID userId) {
        return findUser(userId).getAddresses().stream()
                .map(this::mapAddress)
                .collect(Collectors.toList());
    }

    @Override
    public AddressResponse addAddress(UUID userId, CreateAddressRequest req) {
        User user = findUser(userId);
        UserAddress address = UserAddress.builder()
                .user(user)
                .label(req.getLabel())
                .addressLine1(req.getAddressLine1())
                .addressLine2(req.getAddressLine2())
                .landmark(req.getLandmark())
                .city(req.getCity())
                .state(req.getState())
                .pincode(req.getPincode())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .defaultAddress(user.getAddresses().isEmpty())  // first address = default
                .build();
        user.getAddresses().add(address);
        userRepository.save(user);
        return mapAddress(address);
    }

    @Override
    public AddressResponse updateAddress(UUID userId, UUID addressId, CreateAddressRequest req) {
        User user = findUser(userId);
        UserAddress addr = user.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (req.getLabel()        != null) addr.setLabel(req.getLabel());
        if (req.getAddressLine1() != null) addr.setAddressLine1(req.getAddressLine1());
        addr.setAddressLine2(req.getAddressLine2());
        addr.setLandmark(req.getLandmark());
        if (req.getCity()    != null) addr.setCity(req.getCity());
        if (req.getState()   != null) addr.setState(req.getState());
        if (req.getPincode() != null) addr.setPincode(req.getPincode());
        addr.setLatitude(req.getLatitude());
        addr.setLongitude(req.getLongitude());
        userRepository.save(user);
        return mapAddress(addr);
    }

    @Override
    public void deleteAddress(UUID userId, UUID addressId) {
        User user = findUser(userId);
        user.getAddresses().removeIf(a -> a.getId().equals(addressId));
        userRepository.save(user);
    }

    @Override
    public void setDefaultAddress(UUID userId, UUID addressId) {
        User user = findUser(userId);
        user.getAddresses().forEach(a -> a.setDefaultAddress(a.getId().equals(addressId)));
        userRepository.save(user);
    }

    @Override
    public void updateFcmToken(UUID userId, String token) {
        userRepository.updateFcmToken(userId, token);
    }

    // ── Helpers ──

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserProfileResponse mapToResponse(User u) {
        return UserProfileResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .phone(u.getPhone())
                .email(u.getEmail())
                .avatarUrl(u.getAvatarUrl())
                .dateOfBirth(u.getDateOfBirth())
                .gender(u.getGender() != null ? u.getGender().name() : null)
                .role(u.getRole() != null ? u.getRole().getName() : "CUSTOMER")
                .verified(u.isVerified())
                .createdAt(u.getCreatedAt())
                .build();
    }

    private AddressResponse mapAddress(UserAddress a) {
        return AddressResponse.builder()
                .id(a.getId())
                .label(a.getLabel())
                .addressLine1(a.getAddressLine1())
                .addressLine2(a.getAddressLine2())
                .landmark(a.getLandmark())
                .city(a.getCity())
                .state(a.getState())
                .pincode(a.getPincode())
                .latitude(a.getLatitude())
                .longitude(a.getLongitude())
                .isDefault(a.isDefaultAddress())
                .build();
    }
}
