package com.alivos.api.service;

import com.alivos.api.dto.MyTaskDto;
import com.alivos.api.dto.TaskCommentDto;
import com.alivos.api.dto.TaskCommentRequest;
import com.alivos.api.dto.TaskReviewRequest;
import com.alivos.api.dto.TaskSubmissionDto;
import com.alivos.api.entity.Lesson;
import com.alivos.api.entity.TaskComment;
import com.alivos.api.entity.TaskSubmission;
import com.alivos.api.entity.TaskSubmissionStatus;
import com.alivos.api.entity.User;
import com.alivos.api.exception.ApiException;
import com.alivos.api.repository.LessonRepository;
import com.alivos.api.repository.TaskCommentRepository;
import com.alivos.api.repository.TaskSubmissionRepository;
import com.alivos.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskSubmissionService {

    private final TaskSubmissionRepository taskSubmissionRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TaskSubmissionDto> listTasks() {
        List<TaskSubmission> submissions = taskSubmissionRepository.findAllByOrderByDeliveredAtDesc();
        Map<String, List<TaskComment>> commentsBySubmissionId = commentsFor(submissions);
        return submissions.stream()
                .map(t -> toDto(t, commentsBySubmissionId.getOrDefault(t.getId(), List.of())))
                .toList();
    }

    @Transactional
    public TaskSubmissionDto reviewTask(String id, String adminId, TaskReviewRequest input) {
        TaskSubmission task = taskSubmissionRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Tarea no encontrada"));
        task.setStatus(input.getStatus());
        task.setReviewedAt(Instant.now());
        task = taskSubmissionRepository.save(task);

        if (input.getAdminComment() != null && !input.getAdminComment().isBlank()) {
            addComment(task, adminId, input.getAdminComment(), null);
        }

        List<TaskComment> comments = taskCommentRepository.findByTaskSubmissionIdOrderByCreatedAtAsc(task.getId());
        return toDto(task, comments);
    }

    @Transactional
    public TaskSubmissionDto addAdminComment(String taskSubmissionId, String adminId, TaskCommentRequest input) {
        if (isBlank(input.getText()) && isBlank(input.getFileUrl())) {
            throw ApiException.badRequest("Escribe un comentario o adjunta un archivo");
        }
        TaskSubmission task = taskSubmissionRepository.findById(taskSubmissionId)
                .orElseThrow(() -> ApiException.notFound("Tarea no encontrada"));
        addComment(task, adminId, input.getText(), input.getFileUrl());
        List<TaskComment> comments = taskCommentRepository.findByTaskSubmissionIdOrderByCreatedAtAsc(task.getId());
        return toDto(task, comments);
    }

    @Transactional(readOnly = true)
    public MyTaskDto getMyTask(String userId, String lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> ApiException.notFound("Lección no encontrada"));
        if (!Boolean.TRUE.equals(lesson.getHasTask())) {
            throw ApiException.badRequest("Esta lección no tiene una tarea asociada");
        }

        TaskSubmission task = taskSubmissionRepository.findFirstByUserIdAndLessonId(userId, lessonId).orElse(null);
        if (task == null) {
            return new MyTaskDto(lesson.getTaskDescription(), lesson.getPdfUrl(), lesson.getImageUrl(),
                    null, List.of(), null, null);
        }

        List<TaskComment> comments = taskCommentRepository.findByTaskSubmissionIdOrderByCreatedAtAsc(task.getId());
        return new MyTaskDto(
                lesson.getTaskDescription(),
                lesson.getPdfUrl(),
                lesson.getImageUrl(),
                task.getStatus(),
                comments.stream().map(this::toCommentDto).toList(),
                task.getDeliveredAt(),
                task.getReviewedAt()
        );
    }

    @Transactional
    public MyTaskDto addStudentComment(String userId, String lessonId, TaskCommentRequest input) {
        if (isBlank(input.getText()) && isBlank(input.getFileUrl())) {
            throw ApiException.badRequest("Escribe un comentario o adjunta un archivo");
        }

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

        if (task.getId() == null || task.getStatus() == null
                || task.getStatus() == TaskSubmissionStatus.PENDING
                || task.getStatus() == TaskSubmissionStatus.NEEDS_CORRECTION) {
            task.setStatus(TaskSubmissionStatus.DELIVERED);
            task.setDeliveredAt(Instant.now());
            task.setReviewedAt(null);
        }
        task = taskSubmissionRepository.save(task);

        addComment(task, userId, input.getText(), input.getFileUrl());

        List<TaskComment> comments = taskCommentRepository.findByTaskSubmissionIdOrderByCreatedAtAsc(task.getId());
        return new MyTaskDto(
                lesson.getTaskDescription(),
                lesson.getPdfUrl(),
                lesson.getImageUrl(),
                task.getStatus(),
                comments.stream().map(this::toCommentDto).toList(),
                task.getDeliveredAt(),
                task.getReviewedAt()
        );
    }

    private void addComment(TaskSubmission task, String authorId, String text, String fileUrl) {
        TaskComment comment = new TaskComment();
        comment.setTaskSubmission(task);
        comment.setAuthor(userRepository.getReferenceById(authorId));
        comment.setText(text);
        comment.setFileUrl(fileUrl);
        taskCommentRepository.save(comment);
    }

    private Map<String, List<TaskComment>> commentsFor(List<TaskSubmission> submissions) {
        if (submissions.isEmpty()) return Map.of();
        List<String> ids = submissions.stream().map(TaskSubmission::getId).toList();
        return taskCommentRepository.findByTaskSubmissionIdInOrderByCreatedAtAsc(ids).stream()
                .collect(Collectors.groupingBy(c -> c.getTaskSubmission().getId()));
    }

    private TaskCommentDto toCommentDto(TaskComment c) {
        User author = c.getAuthor();
        return new TaskCommentDto(c.getId(), author.getName(), author.getRole(), c.getText(), c.getFileUrl(), c.getCreatedAt());
    }

    private TaskSubmissionDto toDto(TaskSubmission t, List<TaskComment> comments) {
        List<TaskCommentDto> commentDtos = comments.stream().map(this::toCommentDto).toList();
        return new TaskSubmissionDto(
                t.getId(),
                t.getUser().getName(),
                t.getUser().getEmail(),
                t.getLesson().getTitle(),
                t.getLesson().getModule().getCourse().getTitle(),
                t.getLesson().getTaskDescription(),
                t.getLesson().getPdfUrl(),
                t.getLesson().getImageUrl(),
                t.getStatus(),
                commentDtos,
                t.getDeliveredAt(),
                t.getReviewedAt()
        );
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
