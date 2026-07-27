package com.alivos.api.controller;

import com.alivos.api.dto.CourseReviewDto;
import com.alivos.api.dto.CourseReviewRequest;
import com.alivos.api.security.SecurityUtils;
import com.alivos.api.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses/{slug}/reviews")
@RequiredArgsConstructor
public class CourseReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public CourseReviewDto submitReview(
            @PathVariable String slug,
            @Valid @RequestBody CourseReviewRequest request,
            Authentication authentication
    ) {
        String userId = SecurityUtils.requireUserId(authentication);
        return reviewService.submitReview(userId, slug, request);
    }
}
