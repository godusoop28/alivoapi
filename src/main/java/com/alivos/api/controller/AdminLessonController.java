package com.alivos.api.controller;

import com.alivos.api.dto.FormResponseDto;
import com.alivos.api.dto.LessonDto;
import com.alivos.api.dto.LessonRequest;
import com.alivos.api.service.FormResponseService;
import com.alivos.api.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminLessonController {

    private final LessonService lessonService;
    private final FormResponseService formResponseService;

    @PostMapping("/modules/{moduleId}/lessons")
    public Map<String, LessonDto> createLesson(@PathVariable String moduleId, @RequestBody LessonRequest request) {
        return Map.of("lesson", lessonService.createLesson(moduleId, request));
    }

    @PatchMapping("/lessons/{id}")
    public Map<String, LessonDto> updateLesson(@PathVariable String id, @RequestBody LessonRequest request) {
        return Map.of("lesson", lessonService.updateLesson(id, request));
    }

    @DeleteMapping("/lessons/{id}")
    public Map<String, Boolean> deleteLesson(@PathVariable String id) {
        lessonService.deleteLesson(id);
        return Map.of("ok", true);
    }

    @GetMapping("/lessons/{id}/form-responses")
    public Map<String, List<FormResponseDto>> listFormResponses(@PathVariable String id) {
        return Map.of("responses", formResponseService.listResponses(id));
    }
}
