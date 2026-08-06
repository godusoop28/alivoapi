package com.alivos.api.controller;

import com.alivos.api.dto.ProfessionalDto;
import com.alivos.api.dto.ProfessionalRequest;
import com.alivos.api.service.ProfessionalService;
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
@RequestMapping("/api/admin/professionals")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProfessionalController {

    private final ProfessionalService professionalService;

    @GetMapping
    public Map<String, List<ProfessionalDto>> listProfessionals() {
        return Map.of("professionals", professionalService.listAll());
    }

    @PostMapping
    public Map<String, ProfessionalDto> createProfessional(@RequestBody ProfessionalRequest request) {
        return Map.of("professional", professionalService.create(request));
    }

    @PatchMapping("/{id}")
    public Map<String, ProfessionalDto> updateProfessional(@PathVariable String id, @RequestBody ProfessionalRequest request) {
        return Map.of("professional", professionalService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Map<String, Boolean> deactivateProfessional(@PathVariable String id) {
        professionalService.deactivate(id);
        return Map.of("ok", true);
    }
}
