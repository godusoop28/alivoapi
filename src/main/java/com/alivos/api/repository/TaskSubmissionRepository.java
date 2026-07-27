package com.alivos.api.repository;

import com.alivos.api.entity.TaskSubmission;
import com.alivos.api.entity.TaskSubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, String> {
    List<TaskSubmission> findAllByOrderByDeliveredAtDesc();
    List<TaskSubmission> findByUserIdOrderByDeliveredAtDesc(String userId);
    List<TaskSubmission> findByLessonId(String lessonId);
    Optional<TaskSubmission> findFirstByUserIdAndLessonId(String userId, String lessonId);
    List<TaskSubmission> findByStatusInOrderByDeliveredAtDesc(List<TaskSubmissionStatus> statuses);
}
