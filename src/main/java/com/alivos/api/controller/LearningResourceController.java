package com.alivos.api.controller;

import com.alivos.api.dto.LearningResourceDto;
import com.alivos.api.service.LearningResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class LearningResourceController {

    private final LearningResourceService learningResourceService;

    @GetMapping
    public Map<String, List<LearningResourceDto>> list() {
        return Map.of("resources", learningResourceService.listPublished());
    }
}
