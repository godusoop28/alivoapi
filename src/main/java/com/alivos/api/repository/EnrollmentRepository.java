package com.alivos.api.repository;

import com.alivos.api.entity.Enrollment;
import com.alivos.api.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {
    Optional<Enrollment> findByUserIdAndCourseId(String userId, String courseId);
    List<Enrollment> findByUserIdAndStatus(String userId, EnrollmentStatus status);
    long countByCourseIdAndStatus(String courseId, EnrollmentStatus status);
    List<Enrollment> findByCourseIdAndStatus(String courseId, EnrollmentStatus status);
    List<Enrollment> findByUserId(String userId);
}
