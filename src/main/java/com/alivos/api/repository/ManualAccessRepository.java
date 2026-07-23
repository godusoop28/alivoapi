package com.alivos.api.repository;

import com.alivos.api.entity.ManualAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManualAccessRepository extends JpaRepository<ManualAccess, String> {
    List<ManualAccess> findAllByOrderByCreatedAtDesc();
}
