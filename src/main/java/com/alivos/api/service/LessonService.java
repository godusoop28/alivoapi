package com.alivos.api.service;

import com.alivos.api.dto.LessonAttachmentRequest;
import com.alivos.api.dto.LessonDto;
import com.alivos.api.dto.LessonRequest;
import com.alivos.api.dto.VimeoResolvedDto;
import com.alivos.api.entity.CourseModule;
import com.alivos.api.entity.Enrollment;
import com.alivos.api.entity.Lesson;
import com.alivos.api.entity.LessonAttachment;
import com.alivos.api.entity.LessonAttachmentType;
import com.alivos.api.entity.LessonProgress;
import com.alivos.api.entity.LessonType;
import com.alivos.api.entity.TaskSubmission;
import com.alivos.api.exception.ApiException;
import com.alivos.api.repository.CourseModuleRepository;
import com.alivos.api.repository.EnrollmentRepository;
import com.alivos.api.repository.FormResponseRepository;
import com.alivos.api.repository.LessonAttachmentRepository;
import com.alivos.api.repository.LessonProgressRepository;
import com.alivos.api.repository.LessonRepository;
import com.alivos.api.repository.LessonSurveyResponseRepository;
import com.alivos.api.repository.TaskCommentRepository;
import com.alivos.api.repository.TaskSubmissionRepository;
import com.alivos.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseModuleRepository moduleRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final TaskSubmissionRepository taskSubmissionRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final FormResponseRepository formResponseRepository;
    private final LessonAttachmentRepository lessonAttachmentRepository;
    private final LessonSurveyResponseRepository lessonSurveyResponseRepository;
    private final UserRepository userRepository;
    private final VimeoService vimeoService;

    @Transactional
    public LessonDto createLesson(String moduleId, LessonRequest input) {
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> ApiException.notFound("Módulo no encontrado"));

        long count = lessonRepository.countByModuleId(moduleId);

        Lesson lesson = new Lesson();
        lesson.setModule(module);
        lesson.setTitle(input.getTitle());
        lesson.setType(input.getType() != null ? input.getType() : LessonType.VIDEO);
        lesson.setDescription(input.getDescription());
        lesson.setOrderIndex(input.getOrder() != null ? input.getOrder() : (int) count);
        lesson.setDurationMinutes(input.getDurationMinutes());
        lesson.setVisible(input.getVisible() != null ? input.getVisible() : true);
        lesson.setHasMaterial(input.getHasMaterial() != null ? input.getHasMaterial() : false);
        lesson.setMaterialUrl(input.getMaterialUrl());
        lesson.setHasTask(input.getHasTask() != null ? input.getHasTask() : false);
        lesson.setTaskDescription(input.getTaskDescription());
        lesson.setImageUrl(input.getImageUrl());
        lesson.setPdfUrl(input.getPdfUrl());
        lesson.setAssetType(input.getAssetType());
        lesson.setFormSchema(input.getFormSchema());
        lesson.setChecklistItems(input.getChecklistItems());
        lesson.setCommentsEnabled(input.getCommentsEnabled() != null ? input.getCommentsEnabled() : true);
        lesson.setAdvisoryEnabled(input.getAdvisoryEnabled() != null ? input.getAdvisoryEnabled() : true);

        applyVimeoFields(lesson, input.getVimeoUrl());
        syncAttachments(lesson, input.getAttachments());

        lesson = lessonRepository.save(lesson);
        return CourseMapper.toLessonDto(lesson, java.util.Map.of(), true);
    }

    @Transactional
    public LessonDto updateLesson(String id, LessonRequest input) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Lección no encontrada"));

        if (input.getTitle() != null) lesson.setTitle(input.getTitle());
        if (input.getType() != null) lesson.setType(input.getType());
        if (input.getDescription() != null) lesson.setDescription(input.getDescription());
        if (input.getOrder() != null) lesson.setOrderIndex(input.getOrder());
        if (input.getDurationMinutes() != null) lesson.setDurationMinutes(input.getDurationMinutes());
        if (input.getVisible() != null) lesson.setVisible(input.getVisible());
        if (input.getHasMaterial() != null) lesson.setHasMaterial(input.getHasMaterial());
        if (input.getMaterialUrl() != null) lesson.setMaterialUrl(input.getMaterialUrl());
        if (input.getHasTask() != null) lesson.setHasTask(input.getHasTask());
        if (input.getTaskDescription() != null) lesson.setTaskDescription(input.getTaskDescription());
        if (input.getImageUrl() != null) lesson.setImageUrl(input.getImageUrl());
        if (input.getPdfUrl() != null) lesson.setPdfUrl(input.getPdfUrl());
        if (input.getAssetType() != null) lesson.setAssetType(input.getAssetType());
        if (input.getFormSchema() != null) lesson.setFormSchema(input.getFormSchema());
        if (input.getChecklistItems() != null) lesson.setChecklistItems(input.getChecklistItems());
        if (input.getCommentsEnabled() != null) lesson.setCommentsEnabled(input.getCommentsEnabled());
        if (input.getAdvisoryEnabled() != null) lesson.setAdvisoryEnabled(input.getAdvisoryEnabled());

        if (input.getVimeoUrl() != null) {
            applyVimeoFields(lesson, input.getVimeoUrl());
        }
        if (input.getAttachments() != null) {
            syncAttachments(lesson, input.getAttachments());
        }

        lesson = lessonRepository.save(lesson);
        return CourseMapper.toLessonDto(lesson, java.util.Map.of(), true);
    }

    @Transactional
    public void deleteLesson(String id) {
        if (!lessonRepository.existsById(id)) {
            throw ApiException.notFound("Lección no encontrada");
        }

        List<TaskSubmission> submissions = taskSubmissionRepository.findByLessonId(id);
        if (!submissions.isEmpty()) {
            List<String> submissionIds = submissions.stream().map(TaskSubmission::getId).toList();
            taskCommentRepository.deleteByTaskSubmissionIdIn(submissionIds);
            taskSubmissionRepository.deleteAll(submissions);
        }
        lessonProgressRepository.deleteByLessonId(id);
        formResponseRepository.deleteByLessonId(id);

        List<String> attachmentIds = lessonAttachmentRepository.findByLessonId(id).stream()
                .map(LessonAttachment::getId)
                .toList();
        if (!attachmentIds.isEmpty()) {
            lessonSurveyResponseRepository.deleteByAttachmentIdIn(attachmentIds);
        }
        lessonAttachmentRepository.deleteByLessonId(id);

        lessonRepository.deleteById(id);
    }

    /**
     * Upserts the lesson's attachments by id, keeping the same managed
     * collection instance so Hibernate's orphanRemoval picks up deletions.
     * Any SURVEY attachments dropped from the request have their responses
     * deleted first to avoid a foreign-key violation.
     */
    private void syncAttachments(Lesson lesson, List<LessonAttachmentRequest> requests) {
        Map<String, LessonAttachment> existingById = lesson.getAttachments().stream()
                .filter(a -> a.getId() != null)
                .collect(Collectors.toMap(LessonAttachment::getId, a -> a));

        List<LessonAttachment> next = new ArrayList<>();
        int order = 0;
        for (LessonAttachmentRequest req : requests) {
            LessonAttachment attachment = req.getId() != null ? existingById.get(req.getId()) : null;
            if (attachment == null) {
                attachment = new LessonAttachment();
                attachment.setLesson(lesson);
            }
            attachment.setType(req.getType() != null ? LessonAttachmentType.valueOf(req.getType()) : LessonAttachmentType.PDF);
            attachment.setTitle(req.getTitle());
            attachment.setDescription(req.getDescription());
            attachment.setFileUrl(req.getFileUrl());
            attachment.setExternalUrl(req.getExternalUrl());
            attachment.setFormSchema(req.getFormSchema());
            attachment.setOrderIndex(req.getOrder() != null ? req.getOrder() : order);
            next.add(attachment);
            order++;
        }

        Set<String> keepIds = next.stream()
                .map(LessonAttachment::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<String> removedIds = lesson.getAttachments().stream()
                .filter(a -> a.getId() != null && !keepIds.contains(a.getId()))
                .map(LessonAttachment::getId)
                .toList();
        if (!removedIds.isEmpty()) {
            lessonSurveyResponseRepository.deleteByAttachmentIdIn(removedIds);
        }

        lesson.getAttachments().clear();
        lesson.getAttachments().addAll(next);
    }

    @Transactional
    public void completeLesson(String userId, String lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> ApiException.notFound("Lección no encontrada"));

        LessonProgress progress = lessonProgressRepository.findByUserIdAndLessonId(userId, lessonId)
                .orElseGet(LessonProgress::new);

        if (progress.getUser() == null) {
            progress.setUser(userRepository.getReferenceById(userId));
            progress.setLesson(lesson);
        }
        progress.setCompleted(true);
        progress.setCompletedAt(Instant.now());
        lessonProgressRepository.save(progress);

        recalculateCourseProgress(userId, lesson.getModule().getCourse().getId());
    }

    private void recalculateCourseProgress(String userId, String courseId) {
        List<Lesson> lessons = lessonRepository.findVisibleByCourseId(courseId);
        if (lessons.isEmpty()) return;

        List<String> lessonIds = lessons.stream().map(Lesson::getId).toList();
        long completed = lessonProgressRepository.countCompletedForLessons(userId, lessonIds);

        int progress = Math.round((completed * 100f) / lessons.size());

        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId).orElse(null);
        if (enrollment != null) {
            enrollment.setProgress(progress);
            enrollmentRepository.save(enrollment);
        }
    }

    private void applyVimeoFields(Lesson lesson, String vimeoUrl) {
        if (vimeoUrl == null || vimeoUrl.isBlank()) return;
        VimeoResolvedDto resolved = vimeoService.resolve(vimeoUrl);
        lesson.setVimeoUrl(vimeoUrl);
        lesson.setVimeoId(resolved.getVimeoId());
        lesson.setVimeoEmbedUrl(resolved.getEmbedUrl());
        lesson.setVimeoThumbnail(resolved.getThumbnailUrl());
        if (resolved.getDuration() != null) {
            lesson.setDurationMinutes(resolved.getDuration());
        }
    }
}
