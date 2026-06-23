package com.kiladarbar.service;

import com.kiladarbar.dto.request.UpdateProfileRequest;
import com.kiladarbar.dto.response.AddressResponse;
import com.kiladarbar.dto.response.UserProfileResponse;
import com.kiladarbar.dto.request.CreateAddressRequest;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserProfileResponse getProfile(UUID userId);
    UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request);
    void deleteAccount(UUID userId);

    List<AddressResponse> getAddresses(UUID userId);
    AddressResponse addAddress(UUID userId, CreateAddressRequest request);
    AddressResponse updateAddress(UUID userId, UUID addressId, CreateAddressRequest request);
    void deleteAddress(UUID userId, UUID addressId);
    void setDefaultAddress(UUID userId, UUID addressId);

    void updateFcmToken(UUID userId, String token);
}
