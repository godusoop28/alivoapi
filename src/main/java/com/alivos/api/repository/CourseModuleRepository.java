package com.alivos.api.repository;

import com.alivos.api.entity.CourseModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseModuleRepository extends JpaRepository<CourseModule, String> {
    List<CourseModule> findByCourseIdOrderByOrderIndexAsc(String courseId);
    long countByCourseId(String courseId);
}
