package com.alivos.api.repository;

import com.alivos.api.entity.Purchase;
import com.alivos.api.entity.PurchaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, String> {
    List<Purchase> findAllByOrderByCreatedAtDesc();
    List<Purchase> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Purchase> findByStatusOrderByCreatedAtDesc(PurchaseStatus status);
}
