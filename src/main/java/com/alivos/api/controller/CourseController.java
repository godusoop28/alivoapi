package com.alivos.api.controller;

import com.alivos.api.dto.CourseDto;
import com.alivos.api.security.SecurityUtils;
import com.alivos.api.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public Map<String, List<CourseDto>> listCourses(Authentication authentication) {
        String userId = SecurityUtils.currentUserId(authentication);
        return Map.of("courses", courseService.listPublicCourses(userId));
    }

    @GetMapping("/{slug}")
    public Map<String, CourseDto> getCourse(@PathVariable String slug, Authentication authentication) {
        String userId = SecurityUtils.currentUserId(authentication);
        return Map.of("course", courseService.getCourseBySlug(slug, userId));
    }
}
