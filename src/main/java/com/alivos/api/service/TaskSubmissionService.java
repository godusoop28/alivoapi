package com.alivos.api.service;

import com.alivos.api.dto.TaskReviewRequest;
import com.alivos.api.dto.TaskSubmissionDto;
import com.alivos.api.dto.TaskSubmitRequest;
import com.alivos.api.entity.Lesson;
import com.alivos.api.entity.TaskSubmission;
import com.alivos.api.entity.TaskSubmissionStatus;
import com.alivos.api.exception.ApiException;
import com.alivos.api.repository.LessonRepository;
import com.alivos.api.repository.TaskSubmissionRepository;
import com.alivos.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskSubmissionService {

    private final TaskSubmissionRepository taskSubmissionRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TaskSubmissionDto> listTasks() {
        return taskSubmissionRepository.findAllByOrderByDeliveredAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void reviewTask(String id, TaskReviewRequest input) {
        TaskSubmission task = taskSubmissionRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Tarea no encontrada"));
        task.setStatus(input.getStatus());
        task.setAdminComment(input.getAdminComment());
        task.setReviewedAt(Instant.now());
        taskSubmissionRepository.save(task);
    }

    @Transactional
    public void submitTask(String userId, String lessonId, TaskSubmitRequest input) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> ApiException.notFound("Lección no encontrada"));
        if (!Boolean.TRUE.equals(lesson.getHasTask())) {
            throw ApiException.badRequest("Esta lección no tiene una tarea asociada");
        }

        TaskSubmission task = taskSubmissionRepository.findFirstByUserIdAndLessonId(userId, lessonId)
                .orElseGet(() -> {
                    TaskSubmission created = new TaskSubmission();
                    created.setUser(userRepository.getReferenceById(userId));
                    created.setLesson(lesson);
                    return created;
                });

        task.setAnswer(input.getAnswer());
        task.setFileUrl(input.getFileUrl());
        task.setStatus(TaskSubmissionStatus.DELIVERED);
        task.setDeliveredAt(Instant.now());
        task.setReviewedAt(null);

        taskSubmissionRepository.save(task);
    }

    private TaskSubmissionDto toDto(TaskSubmission t) {
        return new TaskSubmissionDto(
                t.getId(),
                t.getUser().getName(),
                t.getUser().getEmail(),
                t.getLesson().getTitle(),
                t.getLesson().getModule().getCourse().getTitle(),
                t.getLesson().getTaskDescription(),
                t.getAnswer(),
                t.getFileUrl(),
                t.getLesson().getPdfUrl(),
                t.getLesson().getImageUrl(),
                t.getStatus(),
                t.getAdminComment(),
                t.getDeliveredAt(),
                t.getReviewedAt()
        );
    }
}
