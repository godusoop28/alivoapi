package com.alivos.api.controller;

import com.alivos.api.dto.CourseDto;
import com.alivos.api.dto.CourseRequest;
import com.alivos.api.service.CourseService;
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
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCourseController {

    private final CourseService courseService;

    @GetMapping
    public Map<String, List<CourseDto>> listCourses() {
        return Map.of("courses", courseService.listAdminCourses());
    }

    @PostMapping
    public Map<String, CourseDto> createCourse(@RequestBody CourseRequest request) {
        return Map.of("course", courseService.createCourse(request));
    }

    @PatchMapping("/{id}")
    public Map<String, CourseDto> updateCourse(@PathVariable String id, @RequestBody CourseRequest request) {
        return Map.of("course", courseService.updateCourse(id, request));
    }

    @DeleteMapping("/{id}")
    public Map<String, Boolean> deleteCourse(@PathVariable String id) {
        courseService.hideCourse(id);
        return Map.of("ok", true);
    }
}
