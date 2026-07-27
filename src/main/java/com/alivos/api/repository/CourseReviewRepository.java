package com.alivos.api.repository;

import com.alivos.api.entity.CourseReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseReviewRepository extends JpaRepository<CourseReview, String> {
    List<CourseReview> findByCourseIdOrderByCreatedAtDesc(String courseId);
    Optional<CourseReview> findByUserIdAndCourseId(String userId, String courseId);
    long countByCourseId(String courseId);

    @Query("select avg(r.rating) from CourseReview r where r.course.id = :courseId")
    Double averageRatingForCourse(@Param("courseId") String courseId);
}
