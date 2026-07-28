package com.alivos.api.service;

import com.alivos.api.dto.LessonDto;
import com.alivos.api.dto.ModuleDto;
import com.alivos.api.entity.CourseModule;
import com.alivos.api.entity.Lesson;

import java.util.List;
import java.util.Map;

public final class CourseMapper {

    private CourseMapper() {
    }

    public static LessonDto toLessonDto(Lesson lesson, Map<String, Boolean> progressByLessonId, boolean hasAccess) {
        boolean locked = !hasAccess;
        return new LessonDto(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getType(),
                locked ? null : lesson.getDescription(),
                lesson.getOrderIndex(),
                lesson.getDurationMinutes(),
                lesson.getVisible(),
                lesson.getHasMaterial(),
                locked ? null : lesson.getMaterialUrl(),
                lesson.getHasTask(),
                locked ? null : lesson.getTaskDescription(),
                locked ? null : lesson.getVimeoId(),
                locked ? null : lesson.getVimeoUrl(),
                locked ? null : lesson.getVimeoEmbedUrl(),
                lesson.getVimeoThumbnail(),
                lesson.getImageUrl(),
                locked ? null : lesson.getPdfUrl(),
                lesson.getAssetType(),
                locked ? null : lesson.getFormSchema(),
                progressByLessonId.getOrDefault(lesson.getId(), false),
                locked
        );
    }

    public static ModuleDto toModuleDto(CourseModule module, List<LessonDto> lessons) {
        ModuleDto dto = new ModuleDto();
        dto.setId(module.getId());
        dto.setTitle(module.getTitle());
        dto.setDescription(module.getDescription());
        dto.setOrder(module.getOrderIndex());
        dto.setCoverImage(module.getCoverImage());
        dto.setBannerImage(module.getBannerImage());
        dto.setLessons(lessons);
        return dto;
    }
}
