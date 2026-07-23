package com.alivos.api.controller;

import com.alivos.api.dto.TaskReviewRequest;
import com.alivos.api.dto.TaskSubmissionDto;
import com.alivos.api.service.TaskSubmissionService;
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
@RequestMapping("/api/admin/tasks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTaskController {

    private final TaskSubmissionService taskSubmissionService;

    @GetMapping
    public Map<String, List<TaskSubmissionDto>> listTasks() {
        return Map.of("tasks", taskSubmissionService.listTasks());
    }

    @PatchMapping("/{id}/review")
    public Map<String, Boolean> reviewTask(@PathVariable String id, @Valid @RequestBody TaskReviewRequest request) {
        taskSubmissionService.reviewTask(id, request);
        return Map.of("ok", true);
    }
}
