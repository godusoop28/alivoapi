package com.alivos.api.repository;

import com.alivos.api.entity.LessonSurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonSurveyResponseRepository extends JpaRepository<LessonSurveyResponse, String> {
    Optional<LessonSurveyResponse> findByUserIdAndAttachmentId(String userId, String attachmentId);

    List<LessonSurveyResponse> findByAttachmentIdOrderByUpdatedAtDesc(String attachmentId);

    void deleteByAttachmentIdIn(List<String> attachmentIds);
}
