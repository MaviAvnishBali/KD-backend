package com.kiladarbar.service;

import com.kiladarbar.dto.request.PartyHallPackageRequest;
import com.kiladarbar.dto.response.PartyHallPackageResponse;

import java.util.List;
import java.util.UUID;

public interface PartyHallPackageService {
    List<PartyHallPackageResponse> listActive();
    List<PartyHallPackageResponse> adminList();
    PartyHallPackageResponse create(PartyHallPackageRequest request);
    PartyHallPackageResponse update(UUID id, PartyHallPackageRequest request);
    PartyHallPackageResponse toggle(UUID id);
    void delete(UUID id);
}
