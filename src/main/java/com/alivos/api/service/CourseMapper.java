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

    public static LessonDto toLessonDto(Lesson lesson, Map<String, Boolean> progressByLessonId) {
        return new LessonDto(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getType(),
                lesson.getDescription(),
                lesson.getOrderIndex(),
                lesson.getDurationMinutes(),
                lesson.getVisible(),
                lesson.getHasMaterial(),
                lesson.getMaterialUrl(),
                lesson.getHasTask(),
                lesson.getTaskDescription(),
                lesson.getVimeoId(),
                lesson.getVimeoUrl(),
                lesson.getVimeoEmbedUrl(),
                lesson.getVimeoThumbnail(),
                lesson.getImageUrl(),
                lesson.getPdfUrl(),
                lesson.getAssetType(),
                progressByLessonId.getOrDefault(lesson.getId(), false)
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
