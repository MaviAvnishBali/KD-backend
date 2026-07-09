package com.kiladarbar.service.impl;

import com.kiladarbar.dto.request.PartyHallPackageRequest;
import com.kiladarbar.dto.response.PartyHallPackageResponse;
import com.kiladarbar.exception.ResourceNotFoundException;
import com.kiladarbar.model.entity.PartyHallPackage;
import com.kiladarbar.repository.PartyHallPackageRepository;
import com.kiladarbar.service.PartyHallPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyHallPackageServiceImpl implements PartyHallPackageService {

    private final PartyHallPackageRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<PartyHallPackageResponse> listActive() {
        return repository.findByActiveTrueOrderByDisplayOrderAsc().stream().map(this::map).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartyHallPackageResponse> adminList() {
        return repository.findAllByOrderByDisplayOrderAsc().stream().map(this::map).toList();
    }

    @Override
    public PartyHallPackageResponse create(PartyHallPackageRequest req) {
        PartyHallPackage pkg = PartyHallPackage.builder()
                .type(req.getType().trim().toUpperCase())
                .name(req.getName().trim())
                .price(req.getPrice())
                .maxGuests(req.getMaxGuests())
                .emoji(req.getEmoji())
                .tagline(req.getTagline())
                .perks(joinPerks(req.getPerks()))
                .featured(req.isFeatured())
                .active(true)
                .displayOrder((short) req.getDisplayOrder())
                .build();
        return map(repository.save(pkg));
    }

    @Override
    public PartyHallPackageResponse update(UUID id, PartyHallPackageRequest req) {
        PartyHallPackage pkg = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found"));
        pkg.setType(req.getType().trim().toUpperCase());
        pkg.setName(req.getName().trim());
        pkg.setPrice(req.getPrice());
        pkg.setMaxGuests(req.getMaxGuests());
        pkg.setEmoji(req.getEmoji());
        pkg.setTagline(req.getTagline());
        pkg.setPerks(joinPerks(req.getPerks()));
        pkg.setFeatured(req.isFeatured());
        pkg.setDisplayOrder((short) req.getDisplayOrder());
        return map(repository.save(pkg));
    }

    @Override
    public PartyHallPackageResponse toggle(UUID id) {
        PartyHallPackage pkg = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found"));
        pkg.setActive(!pkg.isActive());
        return map(repository.save(pkg));
    }

    @Override
    public void delete(UUID id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Package not found");
        repository.deleteById(id);
    }

    private String joinPerks(List<String> perks) {
        if (perks == null || perks.isEmpty()) return null;
        return perks.stream().map(String::trim).filter(s -> !s.isEmpty()).reduce((a, b) -> a + "\n" + b).orElse(null);
    }

    private List<String> splitPerks(String perks) {
        if (perks == null || perks.isBlank()) return List.of();
        return Arrays.stream(perks.split("\\r?\\n")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private PartyHallPackageResponse map(PartyHallPackage p) {
        return PartyHallPackageResponse.builder()
                .id(p.getId())
                .type(p.getType())
                .name(p.getName())
                .price(p.getPrice())
                .maxGuests(p.getMaxGuests())
                .emoji(p.getEmoji())
                .tagline(p.getTagline())
                .perks(splitPerks(p.getPerks()))
                .featured(p.isFeatured())
                .active(p.isActive())
                .displayOrder(p.getDisplayOrder())
                .build();
    }
}
