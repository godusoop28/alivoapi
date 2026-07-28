package com.alivos.api.repository;

import com.alivos.api.entity.FormResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FormResponseRepository extends JpaRepository<FormResponse, String> {
    Optional<FormResponse> findByUserIdAndLessonId(String userId, String lessonId);
    List<FormResponse> findByLessonIdOrderByUpdatedAtDesc(String lessonId);
    void deleteByLessonId(String lessonId);
}
