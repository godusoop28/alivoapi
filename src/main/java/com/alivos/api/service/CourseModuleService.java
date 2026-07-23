package com.alivos.api.service;

import com.alivos.api.dto.ModuleDto;
import com.alivos.api.dto.ModuleRequest;
import com.alivos.api.entity.Course;
import com.alivos.api.entity.CourseModule;
import com.alivos.api.exception.ApiException;
import com.alivos.api.repository.CourseModuleRepository;
import com.alivos.api.repository.CourseRepository;
import com.alivos.api.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseModuleService {

    private final CourseModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public ModuleDto createModule(String courseId, ModuleRequest input) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> ApiException.notFound("Curso no encontrado"));

        long count = moduleRepository.countByCourseId(courseId);

        CourseModule module = new CourseModule();
        module.setCourse(course);
        module.setTitle(input.getTitle());
        module.setDescription(input.getDescription());
        module.setOrderIndex(input.getOrder() != null ? input.getOrder() : (int) count);
        module.setVisible(input.getVisible() != null ? input.getVisible() : true);
        module.setCoverImage(input.getCoverImage());
        module.setBannerImage(input.getBannerImage());
        module = moduleRepository.save(module);
        return CourseMapper.toModuleDto(module, List.of());
    }

    @Transactional
    public ModuleDto updateModule(String id, ModuleRequest input) {
        CourseModule module = moduleRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Módulo no encontrado"));

        if (input.getTitle() != null) module.setTitle(input.getTitle());
        if (input.getDescription() != null) module.setDescription(input.getDescription());
        if (input.getOrder() != null) module.setOrderIndex(input.getOrder());
        if (input.getVisible() != null) module.setVisible(input.getVisible());
        if (input.getCoverImage() != null) module.setCoverImage(input.getCoverImage());
        if (input.getBannerImage() != null) module.setBannerImage(input.getBannerImage());

        module = moduleRepository.save(module);

        Map<String, Boolean> noProgress = Map.of();
        List<com.alivos.api.dto.LessonDto> lessons = lessonRepository.findByModuleIdOrderByOrderIndexAsc(module.getId()).stream()
                .map(lesson -> CourseMapper.toLessonDto(lesson, noProgress))
                .toList();
        return CourseMapper.toModuleDto(module, lessons);
    }

    @Transactional
    public void deleteModule(String id) {
        if (!moduleRepository.existsById(id)) {
            throw ApiException.notFound("Módulo no encontrado");
        }
        moduleRepository.deleteById(id);
    }
}
