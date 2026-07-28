package com.alivos.api.controller;

import com.alivos.api.dto.LearningResourceDto;
import com.alivos.api.dto.LearningResourceRequest;
import com.alivos.api.service.LearningResourceService;
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
@RequestMapping("/api/admin/resources")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminLearningResourceController {

    private final LearningResourceService learningResourceService;

    @GetMapping
    public Map<String, List<LearningResourceDto>> list() {
        return Map.of("resources", learningResourceService.listAll());
    }

    @PostMapping
    public LearningResourceDto create(@RequestBody LearningResourceRequest request) {
        return learningResourceService.create(request);
    }

    @PatchMapping("/{id}")
    public LearningResourceDto update(@PathVariable String id, @RequestBody LearningResourceRequest request) {
        return learningResourceService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public Map<String, Boolean> delete(@PathVariable String id) {
        learningResourceService.delete(id);
        return Map.of("ok", true);
    }
}
