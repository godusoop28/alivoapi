package com.alivos.api.controller;

import com.alivos.api.dto.CourseDto;
import com.alivos.api.dto.MyTaskDto;
import com.alivos.api.dto.StudentDashboardDto;
import com.alivos.api.dto.TaskCommentRequest;
import com.alivos.api.security.SecurityUtils;
import com.alivos.api.service.CourseService;
import com.alivos.api.service.LessonService;
import com.alivos.api.service.StudentDashboardService;
import com.alivos.api.service.TaskSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StudentController {

    private final CourseService courseService;
    private final StudentDashboardService studentDashboardService;
    private final LessonService lessonService;
    private final TaskSubmissionService taskSubmissionService;

    @GetMapping("/my/courses")
    public Map<String, List<CourseDto>> myCourses(Authentication authentication) {
        String userId = SecurityUtils.requireUserId(authentication);
        return Map.of("courses", courseService.listMyCourses(userId));
    }

    @GetMapping("/my/dashboard")
    public StudentDashboardDto myDashboard(Authentication authentication) {
        String userId = SecurityUtils.requireUserId(authentication);
        return studentDashboardService.getDashboard(userId);
    }

    @PostMapping("/lessons/{lessonId}/complete")
    public Map<String, Boolean> completeLesson(@PathVariable String lessonId, Authentication authentication) {
        String userId = SecurityUtils.requireUserId(authentication);
        lessonService.completeLesson(userId, lessonId);
        return Map.of("ok", true);
    }

    @GetMapping("/tasks/{lessonId}")
    public MyTaskDto getMyTask(@PathVariable String lessonId, Authentication authentication) {
        String userId = SecurityUtils.requireUserId(authentication);
        return taskSubmissionService.getMyTask(userId, lessonId);
    }

    @PostMapping("/tasks/{lessonId}/comments")
    public MyTaskDto addTaskComment(
            @PathVariable String lessonId,
            @RequestBody TaskCommentRequest request,
            Authentication authentication
    ) {
        String userId = SecurityUtils.requireUserId(authentication);
        return taskSubmissionService.addStudentComment(userId, lessonId, request);
    }
}
