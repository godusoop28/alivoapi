package com.alivos.api.repository;

import com.alivos.api.entity.Professional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfessionalRepository extends JpaRepository<Professional, String> {
    List<Professional> findByActiveTrueOrderByNameAsc();
    List<Professional> findAllByOrderByNameAsc();
}
