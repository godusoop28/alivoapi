package com.alivos.api.controller;

import com.alivos.api.dto.FormResponseDto;
import com.alivos.api.dto.FormResponseRequest;
import com.alivos.api.security.SecurityUtils;
import com.alivos.api.service.FormResponseService;
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
@RequestMapping("/api/lessons/{lessonId}/form-response")
@RequiredArgsConstructor
public class FormResponseController {

    private final FormResponseService formResponseService;

    @GetMapping
    public FormResponseDto getMyResponse(@PathVariable String lessonId, Authentication authentication) {
        String userId = SecurityUtils.requireUserId(authentication);
        return formResponseService.getMyResponse(userId, lessonId);
    }

    @PostMapping
    public FormResponseDto submitResponse(
            @PathVariable String lessonId,
            @Valid @RequestBody FormResponseRequest request,
            Authentication authentication
    ) {
        String userId = SecurityUtils.requireUserId(authentication);
        return formResponseService.submitResponse(userId, lessonId, request);
    }
}
