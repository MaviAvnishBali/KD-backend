package com.kiladarbar.repository;

import com.kiladarbar.model.entity.DeliveryPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, UUID> {
    List<DeliveryPartner> findByBranchIdAndAvailableTrue(UUID branchId);
    List<DeliveryPartner> findByAvailableTrue();
}
