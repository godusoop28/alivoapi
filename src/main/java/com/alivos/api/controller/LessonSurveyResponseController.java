package com.alivos.api.controller;

import com.alivos.api.dto.LessonSurveyResponseDto;
import com.alivos.api.dto.LessonSurveyResponseRequest;
import com.alivos.api.security.SecurityUtils;
import com.alivos.api.service.LessonSurveyResponseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lesson-attachments/{attachmentId}/survey-response")
@RequiredArgsConstructor
public class LessonSurveyResponseController {

    private final LessonSurveyResponseService lessonSurveyResponseService;

    @GetMapping
    public LessonSurveyResponseDto getMyResponse(@PathVariable String attachmentId, Authentication authentication) {
        String userId = SecurityUtils.requireUserId(authentication);
        return lessonSurveyResponseService.getMyResponse(userId, attachmentId);
    }

    @PostMapping
    public LessonSurveyResponseDto submitResponse(
            @PathVariable String attachmentId,
            @Valid @RequestBody LessonSurveyResponseRequest request,
            Authentication authentication
    ) {
        String userId = SecurityUtils.requireUserId(authentication);
        return lessonSurveyResponseService.submitResponse(userId, attachmentId, request);
    }
}
