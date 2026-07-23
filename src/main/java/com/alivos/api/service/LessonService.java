package com.alivos.api.service;

import com.alivos.api.dto.LessonDto;
import com.alivos.api.dto.LessonRequest;
import com.alivos.api.dto.VimeoResolvedDto;
import com.alivos.api.entity.CourseModule;
import com.alivos.api.entity.Enrollment;
import com.alivos.api.entity.Lesson;
import com.alivos.api.entity.LessonProgress;
import com.alivos.api.entity.LessonType;
import com.alivos.api.exception.ApiException;
import com.alivos.api.repository.CourseModuleRepository;
import com.alivos.api.repository.EnrollmentRepository;
import com.alivos.api.repository.LessonProgressRepository;
import com.alivos.api.repository.LessonRepository;
import com.alivos.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseModuleRepository moduleRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
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

        applyVimeoFields(lesson, input.getVimeoUrl());

        lesson = lessonRepository.save(lesson);
        return CourseMapper.toLessonDto(lesson, java.util.Map.of());
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

        if (input.getVimeoUrl() != null) {
            applyVimeoFields(lesson, input.getVimeoUrl());
        }

        lesson = lessonRepository.save(lesson);
        return CourseMapper.toLessonDto(lesson, java.util.Map.of());
    }

    @Transactional
    public void deleteLesson(String id) {
        if (!lessonRepository.existsById(id)) {
            throw ApiException.notFound("Lección no encontrada");
        }
        lessonRepository.deleteById(id);
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
