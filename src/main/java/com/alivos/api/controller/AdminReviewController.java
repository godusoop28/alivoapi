package com.alivos.api.controller;

import com.alivos.api.dto.AdminCourseReviewDto;
import com.alivos.api.dto.CourseReviewStatusRequest;
import com.alivos.api.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public Map<String, List<AdminCourseReviewDto>> listReviews() {
        return Map.of("reviews", reviewService.listAllForAdmin());
    }

    @PatchMapping("/{id}/status")
    public Map<String, Boolean> updateStatus(@PathVariable String id, @Valid @RequestBody CourseReviewStatusRequest request) {
        reviewService.updateStatus(id, request.getStatus());
        return Map.of("ok", true);
    }
}
