package com.alivos.api.repository;

import com.alivos.api.entity.Course;
import com.alivos.api.entity.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, String> {
    Optional<Course> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<Course> findByStatusOrderByCreatedAtAsc(CourseStatus status);
    List<Course> findAllByOrderByCreatedAtDesc();
}
