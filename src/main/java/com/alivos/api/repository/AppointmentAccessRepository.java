package com.alivos.api.repository;

import com.alivos.api.entity.AppointmentAccess;
import com.alivos.api.entity.ManualAccessStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppointmentAccessRepository extends JpaRepository<AppointmentAccess, String> {
    List<AppointmentAccess> findAllByOrderByCreatedAtDesc();
    Optional<AppointmentAccess> findFirstByUserIdAndStatusOrderByCreatedAtDesc(String userId, ManualAccessStatus status);
}
