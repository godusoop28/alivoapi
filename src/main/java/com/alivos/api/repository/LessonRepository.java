package com.alivos.api.repository;

import com.alivos.api.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, String> {
    List<Lesson> findByModuleIdOrderByOrderIndexAsc(String moduleId);
    long countByModuleId(String moduleId);

    @Query("select count(l) from Lesson l where l.module.course.id = :courseId and l.visible = true")
    long countVisibleByCourseId(@Param("courseId") String courseId);

    @Query("select l from Lesson l where l.module.course.id = :courseId and l.visible = true")
    List<Lesson> findVisibleByCourseId(@Param("courseId") String courseId);
}
