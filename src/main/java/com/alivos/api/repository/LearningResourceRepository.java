package com.alivos.api.repository;

import com.alivos.api.entity.LearningResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningResourceRepository extends JpaRepository<LearningResource, String> {
    List<LearningResource> findByVisibleTrueOrderByOrderIndexDescCreatedAtDesc();
    List<LearningResource> findAllByOrderByOrderIndexDescCreatedAtDesc();
}
