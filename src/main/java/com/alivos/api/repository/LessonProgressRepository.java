package com.alivos.api.repository;

import com.alivos.api.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, String> {
    Optional<LessonProgress> findByUserIdAndLessonId(String userId, String lessonId);

    @Query("select lp from LessonProgress lp where lp.user.id = :userId and lp.lesson.module.course.id = :courseId")
    List<LessonProgress> findByUserIdAndCourseId(@Param("userId") String userId, @Param("courseId") String courseId);

    long countByUserIdAndCompletedTrue(String userId);

    @Query("select count(lp) from LessonProgress lp where lp.user.id = :userId and lp.completed = true and lp.lesson.id in :lessonIds")
    long countCompletedForLessons(@Param("userId") String userId, @Param("lessonIds") List<String> lessonIds);
}
