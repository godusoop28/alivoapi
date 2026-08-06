package com.alivos.api.repository;

import com.alivos.api.entity.Purchase;
import com.alivos.api.entity.PurchaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, String> {
    List<Purchase> findAllByOrderByCreatedAtDesc();
    List<Purchase> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Purchase> findByStatusOrderByCreatedAtDesc(PurchaseStatus status);
    Optional<Purchase> findFirstByUserIdAndCourseIdAndStatus(String userId, String courseId, PurchaseStatus status);
    Optional<Purchase> findFirstByAppointmentIdOrderByCreatedAtDesc(String appointmentId);
}
