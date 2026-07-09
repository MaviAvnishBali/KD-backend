package com.kiladarbar.repository;

import com.kiladarbar.model.entity.PartyHallPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PartyHallPackageRepository extends JpaRepository<PartyHallPackage, UUID> {

    List<PartyHallPackage> findAllByOrderByDisplayOrderAsc();

    List<PartyHallPackage> findByActiveTrueOrderByDisplayOrderAsc();

    Optional<PartyHallPackage> findByType(String type);
}
