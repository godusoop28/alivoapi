package com.alivos.api.repository;

import com.alivos.api.entity.LessonAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonAttachmentRepository extends JpaRepository<LessonAttachment, String> {
    List<LessonAttachment> findByLessonId(String lessonId);

    void deleteByLessonId(String lessonId);
}
