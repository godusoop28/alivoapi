package com.alivos.api.repository;

import com.alivos.api.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskCommentRepository extends JpaRepository<TaskComment, String> {
    List<TaskComment> findByTaskSubmissionIdOrderByCreatedAtAsc(String taskSubmissionId);
    List<TaskComment> findByTaskSubmissionIdInOrderByCreatedAtAsc(List<String> taskSubmissionIds);
    void deleteByTaskSubmissionIdIn(List<String> taskSubmissionIds);
}
