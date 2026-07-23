package com.alivos.api.controller;

import com.alivos.api.dto.ModuleDto;
import com.alivos.api.dto.ModuleRequest;
import com.alivos.api.service.CourseModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminModuleController {

    private final CourseModuleService courseModuleService;

    @PostMapping("/courses/{courseId}/modules")
    public Map<String, ModuleDto> createModule(@PathVariable String courseId, @RequestBody ModuleRequest request) {
        return Map.of("module", courseModuleService.createModule(courseId, request));
    }

    @PatchMapping("/modules/{id}")
    public Map<String, ModuleDto> updateModule(@PathVariable String id, @RequestBody ModuleRequest request) {
        return Map.of("module", courseModuleService.updateModule(id, request));
    }

    @DeleteMapping("/modules/{id}")
    public Map<String, Boolean> deleteModule(@PathVariable String id) {
        courseModuleService.deleteModule(id);
        return Map.of("ok", true);
    }
}
