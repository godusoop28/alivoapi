package com.alivos.api.repository;

import com.alivos.api.entity.CourseReview;
import com.alivos.api.entity.CourseReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseReviewRepository extends JpaRepository<CourseReview, String> {
    List<CourseReview> findByCourseIdOrderByCreatedAtDesc(String courseId);
    List<CourseReview> findByCourseIdAndStatusOrderByCreatedAtDesc(String courseId, CourseReviewStatus status);
    List<CourseReview> findAllByOrderByCreatedAtDesc();
    Optional<CourseReview> findByUserIdAndCourseId(String userId, String courseId);
    long countByCourseId(String courseId);

    @Query("select avg(r.rating) from CourseReview r where r.course.id = :courseId and r.status = 'APPROVED'")
    Double averageRatingForCourse(@Param("courseId") String courseId);

    @Query("select r from CourseReview r where r.status = 'APPROVED' and r.comment is not null and r.comment <> '' order by r.rating desc, r.createdAt desc")
    List<CourseReview> findApprovedWithCommentOrderByRatingDesc();
}
