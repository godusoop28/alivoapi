package com.alivos.api.controller;

import com.alivos.api.dto.LessonSurveyResponseDto;
import com.alivos.api.service.LessonSurveyResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/lesson-attachments/{attachmentId}/survey-responses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminLessonSurveyController {

    private final LessonSurveyResponseService lessonSurveyResponseService;

    @GetMapping
    public Map<String, List<LessonSurveyResponseDto>> listResponses(@PathVariable String attachmentId) {
        return Map.of("responses", lessonSurveyResponseService.listResponses(attachmentId));
    }
}
